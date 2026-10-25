# GitHub PR・Actions 設定

参照元: `hexqua/apprentice_codex` の `main`。2026-10-26 に API で実設定を確認した。移植先は `hexqua/constellation_n_spells`。

## マージと CI

- merge commit のみ許可。rebase merge と squash merge は無効。
- バージョン更新を含むすべての PR を merge commit で取り込む。参照元のバージョン更新時の rebase merge 例外は採用しない。マージ後の作業ブランチ自動削除は有効、auto merge は無効。
- `.github/workflows/pr-ci.yml` は `main` 向け `pull_request` のみ実行する。`pull_request_target` は使用しない。
- `build` と `gametest` の独立ジョブ、Java 21、secrets 不使用、contents read、checkout 認証情報の非永続化、wrapper 検証、action の完全 SHA 固定を参照元に合わせる。
- Actions は GitHub 所有 action と `gradle/actions/wrapper-validation@50e97c2cd7a37755bbfafc9c5b7cafaece252f6e` のみ許可。verified creator の一括許可は無効。SHA 固定を必須にする。
- 既定 workflow 権限は read、Actions による PR 作成・承認は許可しない。

## 保護ルールとバイパス

参照元の `main-pr-ci-protection` の実値:

- 対象 `refs/heads/main`、enforcement active。
- 削除と force push を禁止、PR を必須。
- required checks: `build` / `gametest`（GitHub Actions、integration ID 15368）。最新 base への追従を必須。
- merge methods は merge / rebase。必須 approval 数は 0。stale approval の破棄、code owner review、last push approval、conversation resolution は無効。
- バイパスは RepositoryRole 5（管理者）の `pull_request` モードのみ。直接 push のバイパスは許可しない。

移植先では上記のうち allowed merge methods を `merge` のみに変更する。参照元の `rebase` 許可は引き継がない。

移植先は非公開リポジトリで、Ruleset と従来の branch protection の両 API が「GitHub Pro へアップグレード、または公開リポジトリにする必要がある」という 403 を返した。このため保護・必須チェック・管理者 PR バイパスは未適用。公開範囲は変更していない。利用可能になるまでは PR・CI の確認を運用で守る。

## workflow 反映後の bootstrap

今回のローカルファイルは未コミットであり、リモートにはまだ反映されていない。Actions の SHA 固定・許可設定は適用済みのため、タグ参照と setup-gradle を使う既存の `build.yml` はこの設定に適合しない。次の PR では同ファイルを削除し、移植した `pr-ci.yml` へ置き換える差分をまとめて反映する。

1. この workflow を含む `main` 向け PR を作成する。required checks はまだ設定しない。
2. `PR CI / build` と `PR CI / gametest` の成功を確認して merge commit で取り込む。
3. `main` 上に workflow があることを確認する。
4. プラン制約が解消した後、上記 Ruleset と管理者 PR バイパスを作成する。allowed merge methods は `merge` のみとする。required check の context は `build` / `gametest` とし、表示名の `PR CI /` を含めない。

workflow の導入前に required check を設定すると、未報告の check 待ちでマージできなくなる場合がある。

## Codex Cloud

参照元文書のスマートトリガーレビュー運用を引き継ぐ場合は、Codex Cloud 側でこのリポジトリへのアクセスとレビュー設定を確認する。今回 GitHub API で設定完了は確認できていない。レビューは CI・人間の判断の補助とし、未確認を実施済みと扱わない。

外部レビューの優先度はそのまま採用せず、[レビュー方針](review-policy.md) の実害基準で再評価する。
