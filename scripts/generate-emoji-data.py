#!/usr/bin/env python3

import argparse
from pathlib import Path

GROUP_TARGETS = [
    [
        "slightly smiling face",
        "grinning face",
        "rolling on the floor laughing",
        "nerd face",
        "smiling face with sunglasses",
        "face with tongue",
        "winking face",
    ],
    [
        "slightly frowning face",
        "crying face",
        "loudly crying face",
        "face screaming in fear",
        "astonished face",
        "flushed face",
        "neutral face",
        "angry face",
    ],
    [
        "thumbs up",
        "waving hand",
        "victory hand",
        "clapping hands",
        "vulcan salute",
        "sign of the horns",
        "handshake",
        "flexed biceps",
        "thumbs down",
    ],
    [
        "red heart",
        "smiling face with open hands",
        "smiling face with heart-eyes",
        "face blowing a kiss",
        "smiling face with halo",
        "smiling face with horns",
        "beer mug",
        "party popper",
        "yawning face",
        "thinking face",
        "cold face",
        "grimacing face",
    ],
]


def parse_emoji_test(source_path: Path) -> dict[str, str]:
    by_name: dict[str, str] = {}
    with source_path.open("r", encoding="utf-8") as source:
        for line in source:
            line = line.strip()
            if not line or line.startswith("#") or ";" not in line or "#" not in line:
                continue

            raw_codepoints, right = [part.strip() for part in line.split(";", 1)]
            status, metadata = [part.strip() for part in right.split("#", 1)]
            if status != "fully-qualified":
                continue

            chunks = metadata.split()
            if len(chunks) < 2:
                continue

            emoji = chunks[0]
            name = " ".join(chunks[2:]).strip()
            if not name:
                continue

            if name not in by_name:
                rebuilt = "".join(chr(int(cp, 16)) for cp in raw_codepoints.split())
                by_name[name] = rebuilt if rebuilt == emoji else emoji

    return by_name


def java_literal(text: str) -> str:
    escaped = text.replace("\\", "\\\\").replace('"', '\\"')
    return f'"{escaped}"'


def emit_java(output_path: Path, groups: list[list[str]]) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "package io.github.sspanak.tt9.util.chars;",
        "",
        "final class EmojiDataGenerated {",
        "\tprivate EmojiDataGenerated() {}",
        "",
        "\tstatic final String[][] GROUPS = new String[][] {",
    ]

    for group in groups:
        entries = ", ".join(java_literal(emoji) for emoji in group)
        lines.append(f"\t\tnew String[] {{{entries}}},")

    lines.extend([
        "\t};",
        "}",
        "",
    ])

    output_path.write_text("\n".join(lines), encoding="utf-8")


def resolve_groups(by_name: dict[str, str]) -> list[list[str]]:
    groups: list[list[str]] = []
    missing_names: list[str] = []

    for target_group in GROUP_TARGETS:
        output_group: list[str] = []
        for name in target_group:
            emoji = by_name.get(name)
            if emoji is None:
                missing_names.append(name)
                continue
            output_group.append(emoji)
        groups.append(output_group)

    if missing_names:
        missing = ", ".join(sorted(set(missing_names)))
        raise ValueError(f"Could not find these emoji names in source data: {missing}")

    return groups


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate emoji Java data from Unicode emoji-test.txt")
    parser.add_argument("--source", required=True, help="Path to Unicode emoji-test.txt")
    parser.add_argument("--output", required=True, help="Path to generated Java file")
    args = parser.parse_args()

    by_name = parse_emoji_test(Path(args.source))
    groups = resolve_groups(by_name)
    emit_java(Path(args.output), groups)


if __name__ == "__main__":
    main()
