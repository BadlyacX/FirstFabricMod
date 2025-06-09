# 套件檢查 & 安裝
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

# ========== Gemini 與 Flask Server ========== #
from flask import Flask, request, jsonify
import google.generativeai as genai
import threading

app = Flask(__name__)
genai.configure(api_key="")
model = genai.GenerativeModel("gemini-2.0-flash")

chat = None
log_lines = []  # log 紀錄共用區

def reset_chat():
    global chat
    chat = model.start_chat(history=[])

@app.route("/ask", methods=["POST"])
def ask():
    data = request.json
    question = data.get("question")
    if not question:
        log("[❌] 無效請求：沒有問題")
        return jsonify({"error": "沒有提供問題"}), 400

    log(f"[📩] API 請求問題：{question}")
    try:
        response = model.generate_content(question)
        reply = response.text.strip()
        log(f"[📨] Gemini 回答（API）：{reply[:100]}...")
        return jsonify({"reply": reply})
    except Exception as e:
        log(f"[❌] API 錯誤：{e}")
        return jsonify({"error": str(e)}), 500

@app.route("/reset", methods=["POST"])
def reset():
    reset_chat()
    return jsonify({"status": "reset"}), 200

def log(message):
    log_lines.append(message)
    print(message)
    if gui_ready:
        gui_log(message)

# ========== GUI 部分（tkinter） ========== #
import tkinter as tk

gui_ready = False

def launch_gui():
    global gui_ready
    gui_ready = True

    def send_question():
        question = entry.get()
        if not question.strip(): return
        entry.delete(0, tk.END)
        log(f"[💬] GUI 提問：{question}")
        try:
            response = chat.send_message(question)
            reply = response.text.strip()
            output_box.config(state="normal")
            output_box.insert(tk.END, f"👤 {question}\n🤖 {reply}\n\n")
            output_box.config(state="disabled")
            output_box.see(tk.END)
            log(f"[✅] Gemini 回答（GUI）：{reply[:100]}...")
        except Exception as e:
            log(f"[❌] GUI 問題錯誤：{e}")

    root = tk.Tk()
    root.title("Gemini Console")

    tk.Label(root, text="輸入問題：").pack()
    entry = tk.Entry(root, width=60)
    entry.pack()
    tk.Button(root, text="送出", command=send_question).pack()
    tk.Button(root, text="🔄 Reset 對話", command=reset_chat).pack()

    tk.Label(root, text="Gemini 回答紀錄：").pack()
    output_box = tk.Text(root, height=20, width=80)
    output_box.pack()
    output_box.config(state="disabled")

    def gui_log(msg):
        output_box.config(state="normal")
        output_box.insert(tk.END, f"[Log] {msg}\n")
        output_box.config(state="disabled")
        output_box.see(tk.END)

    globals()["gui_log"] = gui_log  # 綁定給 log() 使用

    root.mainloop()

# ========== 啟動 Flask + GUI ========== #
if __name__ == "__main__":
    print("[🚀] Gemini Flask Server 啟動中...")
    threading.Thread(target=launch_gui, daemon=True).start()
    app.run(port=5000)
    reset_chat()