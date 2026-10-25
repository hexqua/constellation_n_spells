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
