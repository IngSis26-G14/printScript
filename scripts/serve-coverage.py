"""Serve generated Kover reports over HTTP on localhost."""

import argparse
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DASHBOARD = "/build/reports/kover/modules/index.html"
REPORT_ROOTS = [
    PROJECT_ROOT / "build/reports/kover/modules",
    PROJECT_ROOT / "build/reports/kover/merged/html",
    *(
        module / "build/reports/kover/html"
        for module in PROJECT_ROOT.glob("printscript-*")
        if module.is_dir()
    ),
]


class CoverageHandler(SimpleHTTPRequestHandler):
    def send_head(self):
        if urlsplit(self.path).path == "/":
            self.send_response(302)
            self.send_header("Location", DASHBOARD)
            self.end_headers()
            return None

        target = Path(self.translate_path(self.path)).resolve()
        if not any(target.is_relative_to(report) for report in REPORT_ROOTS):
            self.send_error(404)
            return None
        return super().send_head()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=8000)
    args = parser.parse_args()

    if not (PROJECT_ROOT / DASHBOARD.lstrip("/")).is_file():
        parser.error("Generate the reports first: ./gradlew koverModulesHtmlReport")

    handler = partial(CoverageHandler, directory=str(PROJECT_ROOT))
    with ThreadingHTTPServer(("127.0.0.1", args.port), handler) as server:
        print(f"Coverage reports: http://127.0.0.1:{server.server_port}", flush=True)
        print("Press Ctrl+C to stop.", flush=True)
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            pass


if __name__ == "__main__":
    main()
