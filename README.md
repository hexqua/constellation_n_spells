# Constellation 'n Spells

## 概要

Iron's Spells 'n Spellbooks用の小さなアドオンMODです.

### ブランチとサポート方針

- 開発ブランチ `main` では Java 21 を使用します。
- `main` は Minecraft 1.21.1 / NeoForge の主要開発ブランチです。
- Minecraft 1.20.1 / Forge は `1.20.1-main` で保守し、`main` の変更から対象を選んで backport します。
- `build.gradle` では Java toolchain を 21 に固定していますが、`gradlew.bat` 自体が使う JVM は `JAVA_HOME` または `PATH` に依存します。

## 導入方法

- `mods`配下に`jar`を入れればOKです。
- 現時点でIron's Spells 'n Spellbooks及びそれの前提MOD以外の前提MODはありません.

## PR 運用

- `main` への取り込みは PR 経由で行い、`PR CI / build` と `PR CI / gametest` の成功を必須とします。
- `1.20.1-main` への backport も PR 経由で行い、対象コミットと1.20.1固有の補正内容を説明します。
- optional MOD 付きの特殊 GameTest / client 起動は required CI に含めません。必要な変更ではローカルまたは Codex 実行結果を PR コメントや最終報告に残します。
- Codex Cloud のスマートトリガーレビューをレビュー補助として使います。人間の判断と CI 通過を置き換えるものではありません。
- GitHub Actions は `pull_request` でのみ実行し、`pull_request_target` は使いません。
- CI では secrets を使いません。workflow 権限は read-only に固定します。
- PR は `Create a merge commit` を使います。
- `Rebase and merge` 及び `Squash and merge` は使いません。

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
