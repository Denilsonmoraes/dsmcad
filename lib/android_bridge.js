// Ponte Android <-> Simulador Paginação Pro
// Permite que exportações (SVG/DXF) feitas via Blob + <a download> funcionem dentro do WebView.
(function () {
  if (!window.AndroidBridge) return;
  const blobMap = {};
  const origCreate = URL.createObjectURL.bind(URL);
  const origRevoke = URL.revokeObjectURL.bind(URL);
  URL.createObjectURL = function (obj) {
    const url = origCreate(obj);
    if (obj instanceof Blob) blobMap[url] = obj;
    return url;
  };
  URL.revokeObjectURL = function (url) {
    // Atrasa a revogação para permitir a leitura do blob pela ponte
    setTimeout(function () { delete blobMap[url]; origRevoke(url); }, 15000);
  };
  function sendBlob(blob, name) {
    const reader = new FileReader();
    reader.onload = function () {
      const res = String(reader.result || "");
      const b64 = res.substring(res.indexOf(",") + 1);
      window.AndroidBridge.saveFile(name || "arquivo", blob.type || "application/octet-stream", b64);
    };
    reader.readAsDataURL(blob);
  }
  const origClick = HTMLAnchorElement.prototype.click;
  HTMLAnchorElement.prototype.click = function () {
    const href = this.href || "";
    if (this.hasAttribute("download")) {
      const name = this.getAttribute("download") || "arquivo";
      if (href.startsWith("blob:") && blobMap[href]) { sendBlob(blobMap[href], name); return; }
      if (href.startsWith("data:")) {
        const mime = href.substring(5, href.indexOf(";")) || "application/octet-stream";
        window.AndroidBridge.saveFile(name, mime, href.substring(href.indexOf(",") + 1));
        return;
      }
    }
    return origClick.call(this);
  };
})();
