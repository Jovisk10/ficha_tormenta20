# T20 Ficha

Ficha de personagem para Android do RPG **Tormenta 20 — Edição Jogo do Ano**, feita para uma mesa específica. O dono do projeto fala português; responda e escreva comentários, nomes de telas e mensagens em português.

## Escopo das regras

- Só o livro básico **Tormenta 20 — Edição Jogo do Ano**. Nada de suplementos, Dragão Brasil ou regras da casa.
- O PDF do livro fica em `livro/` (fora do Git, nunca faça commit dele). **Página do PDF = página impressa + 6.** No código, cite sempre a página impressa (ex.: `// p. 115`).
- O catálogo cobre só as escolhas da mesa:
  - Classes: Arcanista (Bruxo, Feiticeiro, Mago), Bardo, Bucaneiro, Druida, Guerreiro
  - Raças: Dahllan, Elfo, Humano, Lefou, Qareen
  - Origens: Artista, Assistente de Laboratório, Marujo, Selvagem, Soldado
  - Magias de 1º e 2º círculos
- O app não reproduz o texto do livro: só números de regra, referências de página e resumos curtos escritos por nós.

## Estrutura

Dois módulos Gradle:

- **`rules`** (Kotlin puro, sem Android, Java 11). Pacote `com.jovis.t20.rules`. Motor de regras, catálogo, dados, formato JSON.
  - `Sheet.kt`: `ManualSheet`, a ficha preenchida pelo jogador.
  - `SheetEngine.kt`: as contas do livro. Todo valor calculado é um `DerivedValue` (total + parcelas com nome da fonte), que alimenta as explicações na tela.
  - `Catalog.kt`, `CatalogPowers.kt`, `ClassPowers.kt`: dados do livro.
  - `CatalogApply.kt`: aplica raça, classe, origem, divindade e poderes à ficha.
  - `Dice.kt`, `DiceModels.kt`: rolagens e geometria dos dados 3D.
  - `SheetCodec.kt`, `Json.kt`: JSON sem bibliotecas externas.
- **`app`** (Android, Jetpack Compose, Material 3). Pacote `com.jovis.t20`. Depende de `rules`.
  - `ui/sheet/SheetScreen.kt`, `ui/dice/`, `ui/edit/`, `ui/list/`, `ui/theme/`.
  - `data/SheetRepository.kt`: uma ficha por arquivo JSON.

**Regra de ouro:** `rules` nunca depende do Android nem do `app`. Lógica de jogo vai em `rules`; o `app` só mostra e edita.

## Comandos

No Windows (PowerShell), use `.\gradlew.bat`; no Git Bash, `./gradlew`.

- Testes das regras (rápido, sem emulador): `.\gradlew.bat :rules:test`
- Compilar o app: `.\gradlew.bat :app:compileDebugKotlin`

Se o Gradle reclamar de `JAVA_HOME`, use o Java que vem com o Android Studio:
`$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"`

## Convenções

- Toda regra nova ou corrigida vem com teste em `rules/src/test/java`, citando a página.
- A ficha real da SEDAF (`SampleSheets.kt`, druida dahllan de 3º nível, PV 36) é o caso de referência; os testes dela não podem quebrar.
- Não apague nem renomeie pastas de módulo (`rules/`, `app/`) nem `build.gradle.kts`.
- Mudanças pequenas e focadas. Não reformate arquivos inteiros sem pedido.

## Como trabalhar neste projeto

- Quando o dono perguntar **"onde modificar"**, ele quer instruções (arquivo, trecho e o que trocar), não que você edite.
- Divida o trabalho com os subagentes do projeto (`.claude/agents/`):
  - **regras-t20**: tudo que envolve regras do livro, catálogo e testes do módulo `rules`.
  - **ui-compose**: telas, tema e layout do módulo `app`.
  - **revisor**: confere o trabalho dos outros antes da entrega. **Chame o revisor depois de qualquer mudança**, e só diga que terminou quando ele aprovar ou quando você tiver corrigido o que ele apontou.
- Ao terminar, resuma em poucas linhas o que mudou e sugira a mensagem de commit. Não faça commit nem push sem o dono pedir.
