# Constellation 'n Spells

## 概要

Iron's Spells 'n Spellbooks用の小さなアドオンMODです.

### ブランチとサポート方針

- 開発ブランチ `main` では Java 21 を使用します。
- `main` は Minecraft 1.21.1 / NeoForge の主要開発ブランチです。
- 現在、Minecraft 1.20.1 / Forge の保守・backport 運用は設けていません。
- `build.gradle` では Java toolchain を 21 に固定していますが、`gradlew.bat` 自体が使う JVM は `JAVA_HOME` または `PATH` に依存します。

## 導入方法

- `mods`配下に`jar`を入れればOKです。
- 現時点でIron's Spells 'n Spellbooks及びそれの前提MOD以外の前提MODはありません.

School Power 連携のサーバー設定と同期・拡張方法は [School Power 連携](docs/school-power-sharing.md) を参照してください。

## 開発環境

- Minecraft 1.21.1 / NeoForge 21.1.265 / Java 21
- MOD ID: `constellationandspells`
- バージョン・依存の正は `gradle.properties` と `build.gradle` です。

PowerShell では `JDK21_HOME` を設定するか、JDK 21 を `%USERPROFILE%\.jdks` または `%USERPROFILE%\.gradle\jdks` に配置します。

```powershell
.\scripts\use-java.ps1
./gradlew.bat build
./gradlew.bat runGameTestServer
```

jar は `build/libs` に出力されます。GameTest は初期登録を検査し、専用 world `run/constellation_gametest_clean` を毎回初期化します。手動確認用 `run/world` は保持します。

データ生成は `./gradlew.bat runData`、人間による client 確認は `./gradlew.bat runClient`、IDE 実行構成の同期は `./gradlew.bat neoForgeIdeSync` を使います。client の表示・操作は GameTest では検証できません。

### MOD を追加した実行構成

全構成の共通依存は Iron's Spells 'n Spellbooks、Curios、GeckoLib、Iron's Lib、playerAnimator です。前の3つは `implementation`
、Better Combat / Arms 'n Artifice / Gems 'n Jewelry / Atlas API / JEI / Jade は `compileOnly` でコード参照できます。

| Client タスク           | GameTest タスク                 | 標準から追加される MOD                                                                                                    |
|-------------------------|---------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `runClient`             | `runGameTestServer`             | 標準：共通依存＋MmmMmmMmmMmm＋Moonlight Lib＋JEI＋MezzConfig＋Jade                                                         |
| `runClientGraphic`      | —                               | Sodium、Sodium Dynamic Lights、Chloride、Reese's Sodium Options                                                           |
| `runClientBetterCombat` | `runGameTestServerBetterCombat` | Better Combat、Cloth Config                                                                                               |
| `runClientIrons`        | `runGameTestServerIrons`        | Iron's Arms 'n Artifice、Iron's Gems 'n Jewelry、Atlas API                                                                |
| `runClientCompat`       | `runGameTestServerCompat`       | BetterCombat＋Irons 構成、Apprentice's Codex、Spell's Atlas、Asterism Arcanum、Ace's Spell Utils、AzureLib、ExpandAbility |

ダミー、Moonlight、JEI、MezzConfig、Jade は Client / GameTest の全構成に含みます。それ以外の Optional MOD と描画 MOD は指定構成にだけ追加し、配布 jar
に同梱しません。Apprentice's Codex も公開配布版を取得し、ローカルリポジトリを参照しません。`runServer`
は共通依存だけで実行します。`runData` は素材定義の Codec を使うため、Iron's Gems 'n Jewelry と Atlas API も読み込みます。

JEI は 19.70.0.460（beta）、必須依存の MezzConfig は 0.6.9、Jade は 15.10.6 を使用します（2026-10-27 時点の Minecraft 1.21.1 / NeoForge 対応最新版）。
配布用メタデータでは JEI / Jade をバージョン制限なしの optional 依存としています。Jade に追加の必須依存 MOD はありません。

IDE と Gradle で同じ実行時依存を使うため、空の `devBase` / `devGraphic` / `devBetterCombat` / `devIrons` / `devCompat` / `devData`
source set を使用します。製品コードと GameTest は引き続き `src/main` に置きます。IntelliJ IDEA では Gradle
プロジェクトを再読み込みして追加の Client / GameTestServer 構成と module を反映してください。

Graphic は Sodium 0.8.13、Dynamic Lights 1.0.10、Chloride 1.8.1、Reese's 2.2.4 を使用します。旧 Sodium Options API は Sodium
0.8 / Reese's 2.2 と非互換のため追加しません。この組合せの起動・ビデオ設定画面・動的光源は人間による client 確認対象です。

Sodium の配布 jar は開発環境の JarJar 探索で本体を見失うため、Graphic 構成では公式 Maven の `sodium-neoforge-mod`
を直接読み込みます。外側 jar の補助クラスは `prepareSodiumGraphicShell` が `build/dev-libs` に再構成し、同梱されていた
Forgified Fabric API の4モジュールは個別に追加します。Gradle の実行準備と IDE 同期で補助 jar も生成します。

Asterism の公開依存情報には別 Minecraft 版への指定があるため、前提ライブラリを1.21.1用に固定しています。バージョンと配布ファイル
ID は `gradle.properties` に集約しています。

```powershell
./gradlew.bat runGameTestServerBetterCombat runGameTestServerIrons runGameTestServerCompat
```

各 GameTest は構成ごとの専用 world を毎回初期化し、指定 MOD の読込と初期登録を確認します。追加構成の world 名は
`constellation_gametest_cleanbettercombat` / `constellation_gametest_cleanirons` / `constellation_gametest_cleancompat`
です。同じ run ディレクトリで別 Gradle プロセスの GameTest を同時実行しないでください。

## PR 運用

- 開発規約: [AGENTS.md](AGENTS.md)、[レビュー方針](docs/review-policy.md)
- レビュー・文字コード・検証報告の Skill: [.codex/skills](.codex/skills)
- `main` への取り込みは PR 経由で行い、`PR CI / build` と `PR CI / gametest` の成功を必須とします。
- optional MOD 付きの特殊 GameTest / client 起動は required CI に含めません。対応する構成を追加した場合、必要な変更ではローカルまたは Codex 実行結果を PR コメントや最終報告に残します。
- Codex Cloud のスマートトリガーレビューをレビュー補助として使います。設定・実行の確認状況は下記の GitHub 設定文書を参照してください。人間の判断と CI 通過を置き換えるものではありません。
- GitHub Actions は `pull_request` でのみ実行し、`pull_request_target` は使いません。
- CI では secrets を使いません。workflow 権限は read-only に固定します。
- バージョン更新を含むすべての PR は `Create a merge commit` を使います。
- `Rebase and merge` 及び `Squash and merge` は使いません。
- GitHub の実設定・プラン制約・bootstrap は [GitHub 設定](docs/github-pr-protection.md)、移植範囲は [移植メモ](docs/repository-migration.md) を参照してください。

## ライセンスや使用について

### 許可(Permissions)

- modpackにはご自由にどうぞ
- 前提modにするのもご自由にどうぞ
- リソースパックもご自由にどうぞ
- スクリーンショット、プレイ動画、動画配信も収益化含めご自由にどうぞ

### 禁止(Restrictions)

- `THIRD_PARTY_NOTICES.md`にある素材のライセンスを破るのはやめてください

### License

- Code: MIT (see `LICENSE`).
- Original assets created for this project: CC0-1.0 (free to use).
- Third-party assets: see `THIRD_PARTY_NOTICES.md` for the applicable licenses.
