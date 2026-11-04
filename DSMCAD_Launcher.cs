using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Windows.Forms;

[assembly: AssemblyTitle("DSMCAD BUILD 3D Pro")]
[assembly: AssemblyDescription("DSMCAD Paginacao & BUILD 3D Pro - Conectado ao GitHub")]
[assembly: AssemblyCompany("DSMCAD Pro")]
[assembly: AssemblyProduct("DSMCAD BUILD 3D Pro")]
[assembly: AssemblyVersion("2.0.0.0")]
[assembly: AssemblyFileVersion("2.0.0.0")]

namespace DSMCADLauncher
{
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            // URL oficial no GitHub Pages com timestamp para garantir que SEMPRE carregue a ultima atualizacao sem cache antigo
            long ts = DateTimeOffset.UtcNow.ToUnixTimeSeconds();
            string githubUrl = "https://denilsonmoraes.github.io/dsmcad/?v=" + ts.ToString();

            // Pasta de perfil dedicada para manter os padroes salvos da IA no PC e abrir em janela propria de programa
            string appData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string userDataDir = Path.Combine(appData, "DSMCAD_BUILD_Pro_App");

            string browserExe = FindAppBrowser();

            if (!string.IsNullOrEmpty(browserExe))
            {
                string arguments = string.Format(
                    "--app=\"{0}\" --start-maximized --user-data-dir=\"{1}\" --disable-http-cache --overscroll-history-navigation=0",
                    githubUrl,
                    userDataDir
                );

                ProcessStartInfo psi = new ProcessStartInfo
                {
                    FileName = browserExe,
                    Arguments = arguments,
                    UseShellExecute = false
                };
                try
                {
                    Process.Start(psi);
                    return;
                }
                catch
                {
                    // Fallback abaixo
                }
            }

            // Fallback caso nao encontre Edge/Chrome em caminhos padrao: abre direto pelo sistema
            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = githubUrl,
                    UseShellExecute = true
                });
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "Erro ao iniciar o DSMCAD BUILD Pro:\n" + ex.Message,
                    "DSMCAD BUILD Pro",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
        }

        static string FindAppBrowser()
        {
            string[] candidates = new string[]
            {
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), @"Google\Chrome\Application\chrome.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), @"Google\Chrome\Application\chrome.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), @"Google\Chrome\Application\chrome.exe")
            };

            foreach (string path in candidates)
            {
                if (!string.IsNullOrEmpty(path) && File.Exists(path))
                {
                    return path;
                }
            }
            return null;
        }
    }
}
