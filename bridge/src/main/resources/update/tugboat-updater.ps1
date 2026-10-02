# Copyright (c) 2025 Uncommon Goods LLC
# SPDX-License-Identifier: MPL-2.0

# Detached update helper spawned by the Tugboat Bridge app. Waits for the app
# to exit, installs the new MSI silently, and relaunches the app.
param(
    [Parameter(Mandatory = $true)][int]$AppProcessId,
    [Parameter(Mandatory = $true)][string]$MsiPath,
    [Parameter(Mandatory = $true)][string]$LaunchExe,
    [Parameter(Mandatory = $true)][string]$LogDir
)

$logFile = Join-Path $LogDir "updater.log"
function Write-Log($message) {
    "$(Get-Date -Format o) $message" | Out-File -FilePath $logFile -Append -Encoding utf8
}

Write-Log "Updater started. MSI: $MsiPath. Waiting for app process $AppProcessId to exit."
Wait-Process -Id $AppProcessId -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

$msiLog = Join-Path $LogDir "msi-install.log"
$exitCode = -1
for ($attempt = 1; $attempt -le 3; $attempt++) {
    Write-Log "Running msiexec (attempt $attempt)."
    $process = Start-Process msiexec.exe -Wait -PassThru -ArgumentList "/i", "`"$MsiPath`"", "/qn", "/norestart", "/l*v", "`"$msiLog`""
    $exitCode = $process.ExitCode
    Write-Log "msiexec exited with $exitCode."
    # 1618 = another installation is already in progress; wait and retry.
    if ($exitCode -ne 1618) { break }
    Start-Sleep -Seconds 15
}

# 3010 = success, reboot required.
if ($exitCode -eq 0 -or $exitCode -eq 3010) {
    Write-Log "Install succeeded."
} else {
    Write-Log "Install FAILED with exit code $exitCode (see $msiLog). Relaunching existing version."
}

if (Test-Path $LaunchExe) {
    Write-Log "Relaunching $LaunchExe."
    Start-Process -FilePath $LaunchExe
} else {
    Write-Log "Launcher not found at $LaunchExe; nothing to relaunch."
}
