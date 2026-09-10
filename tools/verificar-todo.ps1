# Recorre todos los tags v2-capNN: para cada uno, compila el
# proyecto Maven (mvn clean package) y, desde el capitulo 8,
# ejecuta tambien mvn test. Es una API REST, no una app de
# consola, asi que "verificar" significa "compila y sus tests
# pasan", no que la salida coincida con un fichero de referencia.
#
# Requiere: git, Maven 3.9+ y Java 21 en el PATH.
# Uso: powershell -ExecutionPolicy Bypass -File tools\verificar-todo.ps1

Set-Location (Join-Path $PSScriptRoot "..")
$rama = git branch --show-current
$fallos = 0

$tags = git tag -l "v2-cap*" | Sort-Object

foreach ($tag in $tags) {
    $numero = [int]($tag -replace "v2-cap", "")

    git checkout -q $tag
    if ($LASTEXITCODE -ne 0) {
        Write-Host "$tag`: no se puede hacer checkout"
        $fallos++
        continue
    }

    if (-not (Test-Path "proyecto\pom.xml")) {
        Write-Host "$tag`: sin codigo todavia (revision de texto) -- OK"
        continue
    }

    Push-Location proyecto
    mvn -q -B clean package -DskipTests *> "$env:TEMP\verificar-vol2-build.log"
    $rcBuild = $LASTEXITCODE
    Pop-Location

    if ($rcBuild -ne 0) {
        Write-Host "$tag`: FALLO de compilacion (ver $env:TEMP\verificar-vol2-build.log)"
        $fallos++
        continue
    }

    if ($numero -ge 8) {
        Push-Location proyecto
        mvn -q -B test *> "$env:TEMP\verificar-vol2-test.log"
        $rcTest = $LASTEXITCODE
        Pop-Location

        if ($rcTest -ne 0) {
            Write-Host "$tag`: compila, pero FALLAN los tests (ver $env:TEMP\verificar-vol2-test.log)"
            $fallos++
            continue
        }
        Write-Host "$tag`: OK (compila y tests en verde)"
    } else {
        Write-Host "$tag`: OK (compila)"
    }

    git checkout -q -- .
    git clean -fdq proyecto
}

git checkout -q $rama
Write-Host "Fallos: $fallos"
exit $fallos
