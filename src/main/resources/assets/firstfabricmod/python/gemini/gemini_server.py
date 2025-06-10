# ---------------- Auto-install missing packages ---------------- #
import subprocess, sys
def pip_install(package):
    subprocess.check_call([sys.executable, "-m", "pip", "install", package])
try:
    import flask
except ImportError:
    pip_install("flask")
try:
    import google.generativeai
except ImportError:
    pip_install("google-generativeai")

# ---------------- Flask + Gemini Init ---------------- #
from flask import Flask, request, make_response, jsonify
import google.generativeai as genai
import threading
import tkinter as tk
from tkinter import simpledialog, messagebox
import json
import os

CONFIG_DIR = os.path.join(os.getcwd(), "config", "firstfabricmod")
API_KEY_FILE = os.path.join(CONFIG_DIR, "gemini_api_key.json")

GEMINI_API_KEY = None
model = None

app = Flask(__name__)

chat = None
gui_ready = False
output_box = None
log_lines = []

# ---------------- Load API Key ---------------- #
def load_api_key():
    global GEMINI_API_KEY
    if not os.path.exists(API_KEY_FILE):
        return None

    try:
        with open(API_KEY_FILE, 'r') as f:
            data = json.load(f)
            key = data.get('api_key')
            if key and key.strip():
                GEMINI_API_KEY = key.strip()
                print(f"API Key loaded from {API_KEY_FILE}")
                return GEMINI_API_KEY
            else:
                print(f"API Key found in {API_KEY_FILE} but is empty.")
                return None
    except (json.JSONDecodeError, IOError) as e:
        print(f"Error loading API Key from {API_KEY_FILE}: {e}")
        return None

def save_api_key(key):
    if not os.path.exists(CONFIG_DIR):
        os.makedirs(CONFIG_DIR)

    try:
        with open(API_KEY_FILE, 'w') as f:
            json.dump({"api_key": key.strip()}, f, indent=4)
        print(f"API Key saved to {API_KEY_FILE}")
        global GEMINI_API_KEY
        GEMINI_API_KEY = key.strip()
        return True
    except IOError as e:
        print(f"Error saving API Key to {API_KEY_FILE}: {e}")
        return False

def prompt_for_api_key():
    root = tk.Tk()
    root.withdraw()

    api_key = simpledialog.askstring(
        "Gemini API Key Required",
        "Please enter your Google Gemini API Key to enable AI chat.\n"
        "You can get one from: https://aistudio.google.com/app/apikey",
        parent=root
    )

    if api_key:
        if save_api_key(api_key):
            messagebox.showinfo("API Key Saved", "Your Gemini API Key has been saved successfully!")
            return api_key
        else:
            messagebox.showerror("Save Error", "Failed to save API Key. Please check permissions or try again.")
            return None
    else:
        messagebox.showwarning("Input Cancelled", "API Key input cancelled. Gemini AI features may be unavailable.")
        return None
# ---------------- Initialize Gemini Model ---------------- #
def initialize_gemini():
    global model, GEMINI_API_KEY

    loaded_key = load_api_key()

    if not loaded_key:
        print("API Key not found or empty. Prompting user for input...")
        GEMINI_API_KEY = prompt_for_api_key()

    if GEMINI_API_KEY:
        try:
            genai.configure(api_key=GEMINI_API_KEY)
            model = genai.GenerativeModel("gemini-2.0-flash")
            print("Gemini model configured successfully.")
        except Exception as e:
            print(f"Failed to configure Gemini model with provided API Key: {e}")
            model = None
            messagebox.showerror("API Key Error", "Failed to configure Gemini model with the provided API Key. Please ensure it's valid.")
    else:
        print("No valid API Key available. Gemini AI functionality will be disabled.")
        model = None

initialize_gemini()

# ---------------- Chat Control ---------------- #
def reset_chat():
    global chat
    chat = model.start_chat(history=[])
    log("[RESET] Gemini chat history has been reset.")

# ---------------- Flask Routes ---------------- #
@app.route("/ask", methods=["POST"])
def ask():
    global chat
    if chat is None:
        reset_chat()
    data = request.json
    question = data.get("question")
    if not question:
        log("[ERROR] Invalid request: no question provided.")
        return jsonify({"error": "No question provided"}), 400
    log(f"[REQUEST] Received question via API: {question}")
    try:
        response = chat.send_message(question)
        reply = response.text.strip()
        log(f"[RESPONSE] Gemini replied (API): {reply[:100]}...")

        resp = make_response(json.dumps({"reply": reply}, ensure_ascii=False))
        resp.headers["Content-Type"] = "application/json; charset=utf-8"
        return resp
    except Exception as e:
        log(f"[ERROR] API processing error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route("/reset", methods=["POST"])
def reset():
    reset_chat()
    return jsonify({"status": "reset"}), 200

@app.route("/shutdown", methods=["POST"])
def shutdown():
    log("[SHUTDOWN] Server shutdown requested. Exiting...")
    os._exit(0)

# ---------------- Logging & GUI Control ---------------- #
def log(message):
    log_lines.append(message)
    print(message)
    if gui_ready:
        gui_log(message)

def gui_log(msg):
    global output_box
    if output_box:
        output_box.config(state="normal")
        output_box.insert(tk.END, f"[Log] {msg}\n", "log")
        output_box.config(state="disabled")
        output_box.see(tk.END)

def on_close():
    log("[EXIT] Gemini Console window was closed. Shutting down server...")
    os._exit(0)

# ---------------- Tkinter GUI ---------------- #
def launch_gui():
    global gui_ready, output_box
    gui_ready = True

    def send_question():
        question = entry.get()
        if not question.strip():
            return
        entry.delete(0, tk.END)
        log(f"[INPUT] Question submitted from GUI: {question}")
        try:
            response = chat.send_message(question)
            reply = response.text.strip()
            output_box.config(state="normal")
            output_box.insert(tk.END, f"User: {question}\n", "user")
            output_box.insert(tk.END, f"Gemini: {reply}\n\n", "gemini")
            output_box.config(state="disabled")
            output_box.see(tk.END)
            log(f"[RESPONSE] Gemini replied (GUI): {reply[:100]}...")
        except Exception as e:
            log(f"[ERROR] GUI error: {e}")

    root = tk.Tk()
    root.title("Gemini Console")
    root.protocol("WM_DELETE_WINDOW", on_close)

    output_box = tk.Text(root, height=20, width=80, wrap="word")
    output_box.pack(expand=True, fill="both")
    output_box.config(state="disabled")
    output_box.tag_configure("user", foreground="blue")
    output_box.tag_configure("gemini", foreground="red")
    output_box.tag_configure("log", foreground="gray")

    tk.Label(root, text="Enter your question:").pack()
    entry = tk.Entry(root, width=60)
    entry.pack()
    entry.bind("<Return>", lambda event: send_question())
    entry.focus_set()

    tk.Button(root, text="Send", command=send_question).pack()
    tk.Button(root, text="Reset Chat", command=reset_chat).pack()
    tk.Button(root, text="Exit Gemini", command=on_close).pack()

    root.mainloop()

# ---------------- Startup ---------------- #
if __name__ == "__main__":
    reset_chat()
    log("[STARTUP] Gemini Flask Server is running...")
    threading.Thread(target=launch_gui, daemon=True).start()
    app.run(port=5000)
