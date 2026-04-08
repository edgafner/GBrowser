#!/bin/bash
# Block edits to files that may contain secrets
input=$(cat)
file_path=$(echo "$input" | jq -r '.tool_input.file_path // .tool_input.path // empty')

# Allow editing hook scripts and test/doc files
case "$file_path" in
  *.claude/hooks/*) exit 0 ;;
  */test/*|*/tests/*|*/testData/*) exit 0 ;;
esac

case "$file_path" in
  *.env|*.env.*|*/.env|*/.env.*)
    echo "BLOCKED: Cannot edit environment file '$file_path' — may contain secrets" >&2
    exit 2 ;;
  *credentials.json|*credentials.xml|*credentials.yaml|*credentials.yml)
    echo "BLOCKED: Cannot edit credentials file '$file_path'" >&2
    exit 2 ;;
  **/signing/**|*.keystore|*.jks|*.p12|*.pfx)
    echo "BLOCKED: Cannot edit signing file '$file_path'" >&2
    exit 2 ;;
  *secret.key|*secret.pem|*.secret)
    echo "BLOCKED: Cannot edit secret file '$file_path'" >&2
    exit 2 ;;
esac
exit 0
