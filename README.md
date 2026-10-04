# T20 Ficha

Ficha de personagem para **Tormenta 20 — Edição Jogo do Ano**, feita para Android.

O app calcula sozinho os valores da ficha (perícias, Defesa, PV, PM, ataques, carga, CD de magia) e **explica de onde vem cada número**: tocar na Defesa mostra a soma de base, Destreza, armadura e cada bônus, com o nome de quem concedeu. As regras seguem o livro, com a página anotada no código.

> Projeto de fã, não oficial. Veja o [aviso legal](#aviso-legal).

## Funcionalidades

**Ficha**
- Nome, atributos, PV e PM sempre visíveis, com botões para gastar e recuperar durante a sessão.
- Seções recolhíveis: Combate, Perícias, Magias, Habilidades e poderes, Equipamento e Anotações, cada uma com um resumo (ex.: "Defesa 13", "6 de 14 espaços").
- Toque em qualquer valor calculado para ver a explicação do cálculo. Bônus que só valem em certas situações (como a Armadura de Allihanna) aparecem separados, fora do total.

**Edição**
- Todos os campos da ficha, com bônus nomeados (ex.: "Vitalidade +2 em Fortitude"), bônus situacionais e bônus que crescem com o nível.
- Catálogo do livro para preencher a ficha com poucos toques:
  - **Classes:** Arcanista (com os caminhos Bruxo, Feiticeiro e Mago), Bardo, Bucaneiro, Druida e Guerreiro. Preenche PV, PM, atributo de magia e perícias fixas.
  - **Raças:** Dahllan, Elfo, Humano, Lefou e Qareen. Adiciona habilidades, deslocamento e bônus.
  - **Origens:** Artista, Assistente de Laboratório, Marujo, Selvagem e Soldado.
  - **Magias de 1º círculo** (53), já filtradas pelo que o personagem pode aprender: tipo (arcana ou divina), escolas escolhidas e círculo liberado pelo nível.
  - **Armas** (40), **armaduras e escudos** (12) e **itens gerais** (mais de 100), com dano, crítico, alcance, preço e espaços.

**Fichas**
- Várias fichas salvas no celular, com lista na tela inicial.
- Salvamento automático do estado de jogo (PV e PM gastos).
- Modo claro e escuro.

## Arquitetura

O projeto tem dois módulos, separando as regras da interface:

```
┌─────────────────────────────┐
│ app  (Android, Compose)     │  telas, navegação, salvamento
└──────────────┬──────────────┘
               │ depende de
┌──────────────▼──────────────┐
│ rules  (Kotlin puro)        │  motor de regras, catálogo, formato de arquivo
└─────────────────────────────┘
```

- **`rules`** não depende do Android, então é testado em segundos, sem emulador. Todo valor calculado é um `DerivedValue`: o total mais a lista de parcelas que o compõem, que é o que alimenta as explicações da ficha.
- **`app`** usa Jetpack Compose com Material 3. Cada ficha é salva como um arquivo JSON no armazenamento interno do app, por meio de um repositório; trocar por um banco de dados no futuro não afeta as telas.

### Principais arquivos

| Módulo | Arquivo | O que faz |
|---|---|---|
| rules | `Sheet.kt` | A ficha preenchida pelo jogador (`ManualSheet`) |
| rules | `SheetEngine.kt` | As contas do livro aplicadas à ficha, com explicação |
| rules | `Catalog.kt` | Magias, armas, armaduras, itens, classes, raças e origens |
| rules | `CatalogApply.kt` | Aplica escolhas do catálogo à ficha e filtra magias |
| rules | `SheetCodec.kt`, `Json.kt` | Conversão da ficha para JSON e de volta |
| rules | `RuleEngine.kt`, `Validator.kt` | Motor automático completo (usado nos testes de validação) |
| app | `ui/sheet/SheetScreen.kt` | Tela da ficha |
| app | `ui/edit/` | Edição e escolha no catálogo |
| app | `ui/list/SheetListScreen.kt` | Lista de fichas |
| app | `data/SheetRepository.kt` | Salvamento das fichas |
| app | `ui/theme/` | Tema, fontes e a borda rasgada |

## Como rodar

**Requisitos:** Android Studio (versão recente) e um celular Android ou o emulador. O Android Studio já inclui o JDK, o Kotlin e o Gradle.

1. Clone o repositório e abra a pasta no Android Studio.
2. Espere a sincronização do Gradle terminar.
3. Selecione a configuração `app` e clique em **Run**.

Na primeira abertura, o app já vem com uma ficha de exemplo.

## Testes

Os testes ficam no módulo `rules` e conferem as regras contra o livro, usando como caso real a ficha de uma personagem da mesa (uma druida dahllan de 3º nível):

```bash
./gradlew :rules:test
```

Ou, no Android Studio, clique com o botão direito em `rules/src/test/java` e escolha **Run Tests**.

## Próximos passos

- [ ] Rolagem de dados direto da ficha (perícias, ataques e dano)
- [ ] Divindades e poderes concedidos
- [ ] Magias de 2º círculo
- [ ] Exportar e importar fichas
- [ ] Layout em duas colunas para dobráveis e tablets
- [ ] Assistente de regras com IA, que consulta o livro (RAG) e sempre cita a página

## Aviso legal

Este é um projeto de fã, sem fins lucrativos, **sem qualquer vínculo com a Jambô Editora**. *Tormenta* e *Tormenta 20* são marcas de seus respectivos donos.

O app **não reproduz o texto do livro**. Ele contém apenas dados de regras (números como dano, custo e alcance), referências de página e resumos curtos escritos para o app. Para as regras completas, é preciso ter o livro *Tormenta 20 — Edição Jogo do Ano*.

As fontes usadas na interface são livres e distribuídas sob a SIL Open Font License; os textos das licenças estão na pasta [`licenses/`](licenses/):

- [Cinzel](https://fonts.google.com/specimen/Cinzel), de Natanael Gama
- [Crimson Pro](https://fonts.google.com/specimen/Crimson+Pro), de Jacques Le Bailly
- [Source Sans 3](https://fonts.google.com/specimen/Source+Sans+3), da Adobe

## Licença

O código ainda não tem uma licença definida.
