# Shell-JAVA

A Unix-like shell written from scratch in Java. It started as the CodeCrafters "Build Your Own Shell" challenge and grew into its own project, with a hand-written lexer, a recursive descent parser, raw-mode line editing, tab completion, pipelines, background jobs, and persistent history.

Built by Ayush K C.

## Features

### Commands
- **Builtins:** `echo`, `exit`, `type`, `pwd`, `cd`, `jobs`, `complete`, `history`
- **External programs:** found by searching every directory in `PATH`, started in the shell's current directory
- **`cd`:** absolute paths, relative paths (`.`, `..`), and `~` / `~/...` for the home directory

### Quoting and escaping
- Single quotes: everything inside is literal
- Double quotes: literal, except `\"` and `\\`
- Backslash outside quotes: makes the next character literal (including spaces, quotes, and operators)
- Quoted and unquoted parts glue together into one word (`"hello"world` → `helloworld`)

### Redirection
| Operator | Meaning |
|---|---|
| `>` / `1>` | write standard output to a file (overwrite) |
| `>>` / `1>>` | append standard output to a file |
| `2>` | write standard error to a file (overwrite) |
| `2>>` | append standard error to a file |

Redirection works for both builtins and external programs. If the same stream is redirected more than once, the last one wins.

### Pipelines
- Connect any number of commands with `|`
- Builtins and external programs can be mixed freely (`echo hi | wc -c`, `ls | type exit`)
- Consecutive external commands run together, so streaming pipelines like `tail -f log.txt | head -n 5` work
- Errors from each command stay on the screen (or go to that command's `2>` file)

### Background jobs
- Run a command in the background with `&`, which prints its job number and process ID
- `jobs` lists running and finished jobs, with `+` / `-` markers for the most recent ones
- Finished jobs are reported as `Done` once, then removed
- Job numbers are reused once the highest ones finish

### Tab completion
- Builtins and executables from `PATH` for the first word
- Files and directories for later words, including nested paths (`cat src/ma<TAB>`)
- Directories complete with a trailing `/`, single matches with a trailing space
- Multiple matches extend to their longest common prefix; if nothing can be added, the first Tab rings the bell and the second lists all matches
- **Programmable completion:** `complete -C <script> <command>` registers a completer script. It receives the command name, the current word, and the previous word as arguments, plus `COMP_LINE` and `COMP_POINT` in its environment. Use `complete -p` to print registrations and `complete -r` to remove them.

### Line editing
- The terminal is switched to raw mode with `stty`, so keys are handled one at a time
- The original terminal settings are saved at startup and restored on exit (including crashes and Ctrl-C) through a shutdown hook
- Backspace, Tab, and Enter are handled by the shell; other control characters are ignored
- Incomplete lines (an unclosed quote, a trailing backslash, or a trailing `|`) continue on the next line with a `> ` prompt

### Syntax errors
Errors point at the exact problem, fish-style:

```
$ echo hi > | wc
shell: expected a filename after >
echo hi > | wc
          ^
```

### History
- Every complete command is added to an in-memory ring buffer (512 entries), so adding and dropping the oldest entry are both O(1)
- Each command is also appended to a YAML history file in `~/.local/share/shell/`, right after it runs, so nothing is lost on a crash
- Each entry records the command, a timestamp, the working directory, and the existing files it referred to

```yaml
- cmd: "cp notes.txt backup.txt"
  when: 1761771300000
  cwd: /home/vic
  paths:
    - "/home/vic/notes.txt"
    - "/home/vic/backup.txt"
```

## How it works

Each line goes through four stages:

```
line reader  →  lexer  →  parser  →  executor
```

1. **Line reader:** reads keys in raw mode, handles editing and tab completion, and returns the finished line.
2. **Lexer:** a hand-written state machine that turns characters into tokens (`WORD`, `PIPE`, `REDIRECT_OUT`, `APPEND_ERR`, `BACKGROUND`, `END`, ...). Quotes and escapes are resolved here, so a quoted `'>'` becomes a plain word, not an operator. Each token records its offset and length for error messages.
3. **Parser:** a recursive descent parser following this grammar:

   ```
   line      →  pipeline ( "&" )?  END
   pipeline  →  command ( "|" command )*
   command   →  ( WORD | redirect )+
   redirect  →  REDIRECT_OP  WORD
   ```

   It produces one record per command (arguments plus its redirects), and reports syntax errors or incomplete lines.
4. **Executor:** runs builtins inside the shell (swapping `System.out` / `System.err` for redirects and pipelines) and external programs with `ProcessBuilder`, grouping consecutive external commands into one pipeline.

## Project structure

| Package | Contents |
|---|---|
| `Shell` | the main loop and the line reader |
| `Token` | the lexer, tokens, and the parser |
| `Builtins` | builtin commands, jobs, and history |
| `Execution` | running external programs and searching `PATH` |

## Building and running

Requirements: a recent JDK (the project is configured for Java 26 with preview features) and Maven, on Linux or macOS (raw mode uses `stty`).

```
./your_program.sh
```

The script builds the project with Maven and starts the shell. Run it from a real terminal: IDE consoles are not real terminals, so raw mode and tab completion won't work there.

## Roadmap

- Up and down arrows to move through history
- `history clear`, clearing only the current session, and deleting matching entries
- fish-style autosuggestions (from history first, then completion), ranked by frequency and recency
- Live syntax highlighting using the lexer's tokens

## Acknowledgments

- [CodeCrafters](https://codecrafters.io/) for the original challenge
- [Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom, which inspired the lexer and parser design
