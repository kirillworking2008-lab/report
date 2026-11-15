#!/usr/bin/env python3
"""Раздача превью дизайна + приём APK от CI.

GET  /<file>        — статика из ./preview
POST /upload?token= — сохранить тело запроса в ./preview/<name>
"""
import os
import pathlib
import socketserver
import urllib.parse
from http.server import SimpleHTTPRequestHandler

ROOT = pathlib.Path(__file__).resolve().parent.parent / "preview"
PORT = int(os.environ.get("PORT", "8090"))
TOKEN = os.environ.get("UPLOAD_TOKEN", "sterzhen")
MAX_BYTES = 120 * 1024 * 1024


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT), **kwargs)

    def do_POST(self):  # noqa: N802
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path != "/upload":
            self.send_error(404, "unknown endpoint")
            return

        query = urllib.parse.parse_qs(parsed.query)
        if query.get("token", [""])[0] != TOKEN:
            self.send_error(403, "bad token")
            return

        length = int(self.headers.get("Content-Length") or 0)
        if length <= 0 or length > MAX_BYTES:
            self.send_error(400, "bad length")
            return

        name = os.path.basename(query.get("name", ["sterzhen.apk"])[0]) or "sterzhen.apk"
        target = ROOT / name
        tmp = ROOT / (name + ".part")

        remaining = length
        with open(tmp, "wb") as fh:
            while remaining > 0:
                chunk = self.rfile.read(min(65536, remaining))
                if not chunk:
                    break
                fh.write(chunk)
                remaining -= len(chunk)
        tmp.replace(target)

        written = target.stat().st_size
        with open(target, "rb") as fh:
            magic = fh.read(2)

        body = f"{name}: {written} bytes, magic={magic!r}\n".encode()
        self.send_response(200 if written == length else 500)
        self.send_header("Content-Type", "text/plain; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args), flush=True)


class Server(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


if __name__ == "__main__":
    ROOT.mkdir(parents=True, exist_ok=True)
    with Server(("0.0.0.0", PORT), Handler) as httpd:
        print(f"serving {ROOT} on 0.0.0.0:{PORT}", flush=True)
        httpd.serve_forever()
