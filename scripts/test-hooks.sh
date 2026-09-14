#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
fixture=$(mktemp -d "${TMPDIR:-/tmp}/printscript-hooks.XXXXXX")
trap 'rm -rf "$fixture"' EXIT HUP INT TERM
repo="$fixture/repo with spaces"
mkdir -p "$repo/scripts" "$repo/.githooks"
cp "$script_dir/install-hooks.sh" "$repo/scripts/"
cp "$script_dir/../.githooks/pre-commit" "$repo/.githooks/"
git init -q "$repo"

cat > "$repo/gradlew" <<'EOF'
#!/bin/sh
set -eu
test "$PWD" = "$(git rev-parse --show-toplevel)"
test "$#" -eq 1
test "$1" = check
if [ "$(uname -s)" = Darwin ]; then
    test "$JAVA_HOME" = "$(/usr/libexec/java_home -v 17)"
fi
echo called > hook-invoked
exit "${HOOK_TEST_EXIT:-0}"
EOF
chmod +x "$repo/gradlew"

for working_dir in "$repo" "$repo/scripts" "$fixture"; do
    (cd "$working_dir" && /bin/sh "$repo/scripts/install-hooks.sh")
    test "$(git -C "$repo" config --local core.hooksPath)" = .githooks
    test -x "$repo/.githooks/pre-commit"
done

# Invoke through Git to verify discovery and whether failures block commits.
# On macOS, simulate a GUI client inheriting an unusable JAVA_HOME.
if [ "$(uname -s)" = Darwin ]; then
    JAVA_HOME="$fixture/unusable-jdk"
    export JAVA_HOME
fi
git -C "$repo" -c user.name=HookTest -c user.email=hook@example.invalid \
    -c commit.gpgsign=false commit --allow-empty -qm "Successful hook"
test -f "$repo/hook-invoked"
head_before=$(git -C "$repo" rev-parse HEAD)
rm "$repo/hook-invoked"
if HOOK_TEST_EXIT=1 git -C "$repo" -c user.name=HookTest \
    -c user.email=hook@example.invalid -c commit.gpgsign=false \
    commit --allow-empty -qm "Must be blocked"; then
    echo "ERROR: failing checks did not block the commit" >&2
    exit 1
fi
test -f "$repo/hook-invoked"
test "$(git -C "$repo" rev-parse HEAD)" = "$head_before"
echo "Hook regression tests passed."
