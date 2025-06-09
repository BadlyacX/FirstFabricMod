# ---------------- 自動安裝缺少套件 ---------------- #
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

# ---------------- Flask + Gemini 初始 ---------------- #
from flask import Flask, request, make_response, jsonify
import google.generativeai as genai
import threading
import tkinter as tk
import json
import os

genai.configure(api_key="i wont tell you my api key :P")
model = genai.GenerativeModel("gemini-2.0-flash")
app = Flask(__name__)

chat = None
gui_ready = False
output_box = None
log_lines = []

# ---------------- Chat 控制 ---------------- #
def reset_chat():
    global chat
    chat = model.start_chat(history=[])
    log("[🔄] 已重置 Gemini 對話歷史。")

# ---------------- Flask 路由 ---------------- #
@app.route("/ask", methods=["POST"])
def ask():
    global chat
    if chat is None:
        reset_chat()
    data = request.json
    question = data.get("question")
    if not question:
        log("[❌] 無效請求：沒有問題")
        return jsonify({"error": "沒有提供問題"}), 400
    log(f"[📩] API 請求問題：{question}")
    try:
        response = chat.send_message(question)
        reply = response.text.strip()
        log(f"[📨] Gemini 回答（API）：{reply[:100]}...")
        
        resp = make_response(json.dumps({"reply": reply}, ensure_ascii=False))
        resp.headers["Content-Type"] = "application/json; charset=utf-8"
        return resp
    except Exception as e:
        log(f"[❌] API 錯誤：{e}")
        return jsonify({"error": str(e)}), 500

@app.route("/reset", methods=["POST"])
def reset():
    reset_chat()
    return jsonify({"status": "reset"}), 200

@app.route("/shutdown", methods=["POST"])
def shutdown():
    log("[🛑] 收到關閉請求，正在終止 Flask 伺服器...")
    os._exit(0)

# ---------------- Log 與 GUI 控制 ---------------- #
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
    log("[❌] 使用者關閉了 Gemini Console，正在終止伺服器...")
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
        log(f"[💬] GUI 提問：{question}")
        try:
            response = chat.send_message(question)
            reply = response.text.strip()
            output_box.config(state="normal")
            output_box.insert(tk.END, f"👤 {question}\n", "user")
            output_box.insert(tk.END, f"🤖 {reply}\n\n", "gemini")
            output_box.config(state="disabled")
            output_box.see(tk.END)
            log(f"[✅] Gemini 回答（GUI）：{reply[:100]}...")
        except Exception as e:
            log(f"[❌] GUI 問題錯誤：{e}")

    root = tk.Tk()
    root.title("Gemini Console")
    root.protocol("WM_DELETE_WINDOW", on_close)

    output_box = tk.Text(root, height=20, width=80, wrap="word")
    output_box.pack(expand=True, fill="both")
    output_box.config(state="disabled")
    output_box.tag_configure("user", foreground="blue")
    output_box.tag_configure("gemini", foreground="red")
    output_box.tag_configure("log", foreground="gray")

    tk.Label(root, text="輸入問題：").pack()
    entry = tk.Entry(root, width=60)
    entry.pack()
    entry.bind("<Return>", lambda event: send_question())
    entry.focus_set()

    tk.Button(root, text="送出", command=send_question).pack()
    tk.Button(root, text="🔄 Reset 對話", command=reset_chat).pack()
    tk.Button(root, text="🛑 關閉 Gemini", command=on_close).pack()

    root.mainloop()

# ---------------- 啟動 ---------------- #
if __name__ == "__main__":
    reset_chat()
    log("[🚀] Gemini Flask Server 啟動中...")
    threading.Thread(target=launch_gui, daemon=True).start()
    app.run(port=5000)
