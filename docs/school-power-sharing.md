# School Power 連携

プレイヤーの Zodiac 魔法は、Zodiac 自身と連携対象 school の **生の Spell Power 属性値の最大値**を使う。属性値自体は変更しない。mob には適用しない。

## サーバー設定

ワールドの `serverconfig/constellationandspells-server.toml` にある `[schoolPowerSharing]` で設定する。新規ワールド向けの初期設定は NeoForge 標準の `defaultconfigs` で配布できる。

- `enabled`（初期値 `true`）: 対象 school → Zodiac の共有を有効にする。
- `autoDetectAstral`（初期値 `true`）: registry ID の path が厳密に `astral` の school を対象にする。
- `additionalSchools`（初期値 `[]`）: 完全な school ID を追加する。Fire 等の Astral 以外も指定可能。
- `excludedSchools`（初期値 `[]`）: 完全な school ID を除外する。追加指定・自動検出より優先。

例えば `asterismarcanum:astral` は自動検出されるが、`example:some_astral` や `example:astral_some` は検出されない。表示名や翻訳は判定に使わない。明示指定には `namespace:path` が必要で、不正形式は NeoForge の設定検証対象となる。未導入 MOD 等の未登録 ID は保持できるが、対象集合には入らない。重複と Zodiac 自身は除外する。

設定ファイルの再読込通知で、稼働中の server の対象集合を再構築する。ファイル監視を NeoForge 側で無効にしている場合は、server を再起動して反映する。装備・効果による属性値の変化は都度参照する。

## 計算と拡張時の参照先

- `SchoolPowerSharing.stateFor(entity)` は論理 side に応じた不変 snapshot を返す。`rules()` と `targets()` を将来の表示・専用アイテムでも参照できる。
- `SchoolPowerSharingState.rawPowerFor(school, entity)` は、その school が保持する power 属性の実値を返す。対象属性を entity が持たない場合は空を返す。school 独自の `getPowerFor()` による追加計算は呼ばない。
- 対象集合は `enabled` と独立する。この boolean は今回の転送方向だけを制御する。
- 将来 Zodiac → 他 school を追加するときも、共有後の Zodiac 有効値ではなく上記の生の値を使う。Astral → Zodiac → 別 Astral という転送は行わない。
- 最大値の下限を `1.0` に固定しない。ただし、実在する対象属性が `1.0` なら、それより低い Zodiac 値に代わって採用される。

## 同期

NeoForge の SERVER 設定同期に加え、ログイン時と server 設定の読込・再読込時に `school_power_sharing` S2C payload で4項目の snapshot を送る。設定適用と送信は server thread、client の snapshot 更新は payload registrar の MAIN handler で行う。

将来の表示は `ServerConfig` の値を直接読むのではなく、`SchoolPowerSharing.stateFor(entity)` を使う。接続中の変更も S2C snapshot が反映し、未受信・切断後は共有無効の状態になる。integrated server と client の状態は別々に保持する。client から server 設定を変更する通信はない。

## 検証範囲

標準 GameTest では名前解決・計算・snapshot の codec と状態差し替えを検証し、既存 compat 構成では Asterism Arcanum | Addon for Iron's Spells and Spellbooks の実 school と属性も確認する。Tempest Almanac - Iron's Spells 'n Spellbooks の依存追加や専用テスト構成は設けない。

実接続での初回同期・接続中の再読込・切断後の別 server 接続・シングルプレイへの切替は、client を使う手動確認対象。現在はゲーム内説明や同期状態を表示する UI を追加していないため、client snapshot の確認にはデバッガ等を用いる。

## 参考資料

- [Asterism Arcanum | Addon for Iron's Spells and Spellbooks（CurseForge）](https://www.curseforge.com/minecraft/mc-mods/asterism-arcanum): 既存 compat 構成の検証対象。school ID は `asterismarcanum:astral`。
- [Tempest Almanac - Iron's Spells 'n Spellbooks（CurseForge）](https://www.curseforge.com/minecraft/mc-mods/tempest-almanac-irons-spells-n-spellbooks): 同じ自動検出条件に該当する参考例。school ID は `tempest_almanac:astral`。今回の GameTest での実 MOD 検証対象には含めない。

これらの MOD 固有 ID は検証・参考情報であり、実装の許可リストとして固定しない。
