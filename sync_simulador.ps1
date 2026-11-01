# Copia o simulador HTML (fonte única de verdade) para os assets do app Android,
# trocando o Tailwind do CDN pela cópia offline e injetando a ponte Android.
$src = "C:\Users\denil\.gemini\antigravity\brain\e9d77b14-a5e1-44fd-accc-2875cfdc9411\simulador_paginacao_pro.html"
$dst = Join-Path $PSScriptRoot "app\src\main\assets\simulador.html"
$html = [System.IO.File]::ReadAllText($src, [System.Text.Encoding]::UTF8)
$html = $html.Replace('https://www.gstatic.com/antigravity/web/dev/tailwindcss.min.js', 'lib/tailwindcss.min.js')
$html = $html.Replace('</head>', "  <script src=`"lib/android_bridge.js`"></script>`n</head>")
[System.IO.File]::WriteAllText($dst, $html, (New-Object System.Text.UTF8Encoding($false)))
Write-Output "Simulador sincronizado: $dst ($((Get-Item $dst).Length) bytes)"
