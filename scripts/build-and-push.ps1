#.\scripts\build-and-push.ps1 -Command stay-board-rms-build-push -Tag v1.0.0
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("stay-board-rms-build-push")]
    [string]$Command,

    [Parameter(Mandatory = $true)]
    [string]$Tag,

    [Parameter(Mandatory = $false)]
    [string]$PropertiesFile = "gradle.properties"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path (Join-Path $scriptDir "..")).Path
$currentDir = (Resolve-Path ".").Path

# Allow execution only from this repository (root or any child folder).
$normalizedProjectRoot = $projectRoot.TrimEnd('\', '/')
$normalizedCurrentDir = $currentDir.TrimEnd('\', '/')
$isInsideProject = $normalizedCurrentDir -eq $normalizedProjectRoot -or $normalizedCurrentDir.StartsWith($normalizedProjectRoot + [System.IO.Path]::DirectorySeparatorChar)

if (-not $isInsideProject) {
    throw "This script can only run inside this project: $projectRoot"
}

function Get-PropertyValue {
    param(
        [string]$Content,
        [string]$Key
    )

    $pattern = "(?m)^\s*$([regex]::Escape($Key))\s*=\s*(.+)\s*$"
    $match = [regex]::Match($Content, $pattern)
    if ($match.Success) {
        return $match.Groups[1].Value.Trim()
    }
    return $null
}

if (!(Test-Path $PropertiesFile)) {
    throw "Properties file not found: $PropertiesFile"
}

$propertiesContent = Get-Content $PropertiesFile -Raw
$dockerHubUsername = Get-PropertyValue -Content $propertiesContent -Key "dockerHubUsername"
$dockerHubPassword = Get-PropertyValue -Content $propertiesContent -Key "dockerHubPassword"
$dockerRepoUrl = Get-PropertyValue -Content $propertiesContent -Key "dockerRepoUrl"

if ([string]::IsNullOrWhiteSpace($dockerHubUsername)) {
    throw "dockerHubUsername not found in $PropertiesFile"
}
if ([string]::IsNullOrWhiteSpace($dockerHubPassword)) {
    throw "dockerHubPassword not found in $PropertiesFile"
}
if ([string]::IsNullOrWhiteSpace($dockerRepoUrl)) {
    $dockerRepoUrl = $dockerHubUsername
}

$backendImage = "$dockerRepoUrl/stay-board-rms:$Tag"

Write-Host "Docker login check..." -ForegroundColor Cyan
docker info | Out-Null

Write-Host "Docker Hub login..." -ForegroundColor Cyan
$dockerHubPassword | docker login --username $dockerHubUsername --password-stdin

Write-Host "Building backend image: $backendImage" -ForegroundColor Yellow
docker build `
  -t $backendImage `
  -f Dockerfile `
  .

Write-Host "Pushing backend image..." -ForegroundColor Green
docker push $backendImage

Write-Host ""
Write-Host "Done." -ForegroundColor Green
Write-Host "Backend : $backendImage"
