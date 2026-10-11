#!/usr/bin/env python3
"""Build an installable plugin against an installed IntelliJ IDEA 2026.2 SDK."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--idea', type=Path, default=Path('/Applications/IntelliJ IDEA.app'),
                        help='IDE installation (.app on macOS, installation directory elsewhere)')
    parser.add_argument('--java-home', type=Path, help='JDK 17 for the language-module Gradle build')
    args = parser.parse_args()
    idea = args.idea.resolve()
    if (idea / 'Contents').is_dir():
        idea /= 'Contents'
    info_path = idea / 'Resources/product-info.json'
    if not info_path.exists():
        info_path = idea / 'product-info.json'
    info = json.loads(info_path.read_text())
    if not info['buildNumber'].startswith('262.'):
        parser.error('This plugin targets IntelliJ 2026.2 (build 262). Supply a matching installation.')
    java_home = args.java_home
    if java_home is None and Path('/usr/libexec/java_home').exists():
        java_home = Path(subprocess.check_output(['/usr/libexec/java_home', '-v', '17'], text=True).strip())
    if java_home is None:
        java_home = Path(os.environ['JAVA_HOME'])
    env = dict(os.environ, JAVA_HOME=str(java_home))
    gradle = 'gradlew.bat' if os.name == 'nt' else 'gradlew'
    subprocess.run([str(ROOT / gradle), ':printscript-editor:check', ':printscript-editor:pluginLibraries'], cwd=ROOT, env=env, check=True)
    java_suffix = '.exe' if os.name == 'nt' else ''
    jbr = idea / ('jbr/Contents/Home' if (idea / 'jbr/Contents/Home').exists() else 'jbr')
    javac = jbr / ('bin/javac' + java_suffix)
    java = jbr / ('bin/java' + java_suffix)
    if not javac.exists():
        parser.error('The IDE must include a JBR with javac (or use an installation with its bundled JDK).')
    module = ROOT / 'printscript-intellij'
    build = module / 'build'
    build.mkdir(exist_ok=True)
    libraries = sorted((ROOT / 'printscript-editor/build/plugin-libraries').glob('*.jar'))
    sdk = sorted((idea / 'lib').glob('*.jar'))
    classpath = os.pathsep.join(str(path) for path in sdk + libraries)
    with tempfile.TemporaryDirectory(prefix='plugin-', dir=build) as staging:
        staging = Path(staging)
        classes = staging / 'classes'
        classes.mkdir()
        sources = sorted((module / 'src/main/java').rglob('*.java'))
        subprocess.run([str(javac), '--release', '25', '-encoding', 'UTF-8', '-cp', classpath,
                        '-d', str(classes), *map(str, sources)], check=True)
        tests = sorted((module / 'src/test/java').rglob('*.java'))
        if tests:
            test_classpath = str(classes) + os.pathsep + classpath
            subprocess.run([str(javac), '--release', '25', '-cp', test_classpath, '-d', str(classes), *map(str, tests)], check=True)
            subprocess.run([str(java), '-ea', '-Djava.awt.headless=true', '-cp', test_classpath,
                            'printscript.idea.PluginSmokeTest'], check=True)
            for path in classes.rglob('*SmokeTest*.class'):
                path.unlink()
        jar = staging / 'printscript-intellij.jar'
        with zipfile.ZipFile(jar, 'w', zipfile.ZIP_DEFLATED) as archive:
            for directory in [classes, module / 'src/main/resources']:
                for path in sorted(directory.rglob('*')):
                    if path.is_file():
                        archive.write(path, path.relative_to(directory).as_posix())
        output = build / 'distributions/printscript-intellij-0.1.0.zip'
        output.parent.mkdir(exist_ok=True)
        with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
            archive.write(jar, 'printscript/lib/' + jar.name)
            for library in libraries:
                # IntelliJ supplies Kotlin stdlib and JetBrains annotations.
                if not library.name.startswith(('kotlin-stdlib', 'annotations-')):
                    archive.write(library, 'printscript/lib/' + library.name)
        print(f'Plugin ZIP: {output}')


if __name__ == '__main__':
    main()
