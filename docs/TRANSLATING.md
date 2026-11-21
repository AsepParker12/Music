# Translating

Music's strings live in this repository, in each module's
`src/main/res/values-<language>/strings.xml` (for example `values-id` for
Indonesian).

To add or fix a translation:

1. Copy the English `values/strings.xml` of the module you want to translate
   into a new `values-<language>` folder, keeping every string name and
   placeholder (`%1$s`, `%d`, …) exactly as they are.
2. Translate the text, then open a pull request with the new files.

There is no external translation service for this project.
