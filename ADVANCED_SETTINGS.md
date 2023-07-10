# 高度な設定

この先はサーバー機本体に影響を及ぼすことが可能になります

そのため**十分な知識を持った方のみ以下の設定を行ってください**

なお、コマンドの前につける文字を他のBotと同じものにしたい場合、他のBotのコマンドを実行する際に「コマンドが見つかりませんでした」というエラーメッセージが表示される場合があります

そのエラーメッセージを表示させたくない場合、[他のBotに存在するコマンド](#他のbotに存在するコマンド)の欄をご覧ください

## システムコマンドの設定
commands.propertiesにコマンドを設定することでDiscordからコマンドを実行することができます

なお、これらのコマンドを使用するためには`commandChannelId`で設定したチャンネル内で`commandRoleId`のロールを持つ人が実行する必要があります

### 基本的な記法

```
command_name_<option>=command
```

オプションは以下の通りです

|オプション|説明|
|---|---|
|f|commandRoleIdが設定されている場合でも誰でも実行できるようになります|
|o|標準出力の内容をコマンドへの返信として送信します|
|e|標準エラー出力の内容をコマンドへの返信として送信します|
|i|このコマンドは無視します|

オプションは順不同で、コマンド名の最後に`_<option>`という形で指定します

そのため、最後が`_fo`などオプションのみで終わるコマンド名は指定できません

### コマンド例

#### ヘルプコマンド
```
help_fo=cat help.txt
```
誰でも使えるhelpコマンドを実装することができます

#### バックアップコマンド
```
backup_oe=tar cvfz `date "+%Y%m%d%H%M"`.tar.gz minecraft/world
```
Discordからバックアップをとることができます

標準出力・標準エラー出力ともに返信します

#### 他のBotに存在するコマンド
```
hoge_i
```
コマンドの前につける文字が「!」で、他のBotに「hoge」というコマンドが存在し、「!hoge」というようにコマンドを実行した際、「コマンドが見つかりませんでした」というエラーメッセージが表示されます

そこで「hoge」コマンドを無視するようにすることで、エラーメッセージを表示しないようにすることができます

また、今回は実行するコマンドがないため「=」ごと省略することができます

## ログフィルター
Discordに送信するログのフィルターを設定します

特定のログをトリガーに任意のコマンドを実行することができます

配布ファイル内に含まれるlog_filter.jsonに追記・編集してください

ホワイトリスト・ブラックリスト両方に一致した場合、ブラックリストのほうが優先されます

また、複数のホワイトリストに一致した場合、上にあるものが優先されます

### 基本的な記法
```
{
    "comment": "コメント",
    "regex": "正規表現",
    "separator": "区切り文字",
    "commands": [
        "このログが出力された際に実行するコマンド1",
        "コマンド2",
        ...
    ],
    "argCount": 引数の数,
    "isWhite": ホワイトリストか,
    "isPlayerLog": プレイヤー固有のログか,
    "isReturnOutputs": [
        コマンド1の標準出力を返すか,
        コマンド2の標準出力を返すか,
        ...
    ],
    "isReturnErrors": [
        コマンド1の標準エラー出力を返すか,
        コマンド2の標準エラー出力を返すか,
        ...
    ]
},
```

|項目|補足|初期値|
|---|---|---|
|comment|jsonにはコメントが存在しないため、この項目を使用してください||
|regex|この正規表現に一致したログをフィルタリングします<br>`player`、`argn`($0\le n \le \mathrm{argCount}$)のグループ名をつけることができます<br>ホワイトリストの場合、これら以外のグループの中身が送信されます||
|separator|グループが複数ある場合、この文字を用いて結合されます|半角空白|
|commands|コマンド内に含まれる`argn`はそのグループの中身に置換されます|なし(空配列)|
|argCount|`argn`の個数を指定します<br>$n$の最大値$+1$になります|0|
|isWhite|true/falseで指定してください|true|
|isPlayerLog|Discordに送信する際にアイコン/表示名がそのプレイヤーのものになります<br>`player`グループが必須になります|false|
|isReturnOutputs|true/falseで指定してください<br>commandsの数と一致している必要があります|なし(空配列)|
|isReturnError|true/falseで指定してください<br>commandsの数と一致している必要があります|なし(空配列)|

`regex`のみ必須で、それ以外の項目を書かなかった場合、初期値が使用されます

<br>

また、commandsでは以下の特別なコマンドを使用することができます
|コマンド名|説明|
|---|---|
|LoginUtil.insertLoginRecord|データベースにログイン履歴を記録します<br>`arg0`を設定する必要があり、その中身をMCIDとして記録します|
|LoginUtil.setId|MinecraftアカウントとDiscordアカウントを紐づけます<br>`isPlayerLog`を`true`にする必要があります<br>`arg0`を設定する必要があり、その中身をIDとして設定します|

### フィルター例
#### Minecraft内から利用できる独自コマンド
```
{
    "comment": "ID設定用コマンド",
    "regex": "\\[.*\\] \\[Server thread/INFO\\]: \\<(?<player>.*)\\> !setid (?<arg0>.*)",
    "commands": [
        "LoginUtil.setId"
    ],
    "argCount": 1,
    "isPlayerLog": true,
    "isReturnOutputs": [
        false
    ],
    "isReturnErrors": [
        false
    ]
},
```
`!setid xxxxxxx`とMinecraft内で送信した場合、LoginUtil.setIdコマンドが使用されます

#### サーバー参加ログ
```
{
    "comment": "サーバー参加ログ",
    "regex": "\\[.*\\] \\[Server thread/INFO\\]: ((?<arg0>[^\\s]*).*joined the game)",
    "argCount": 1,
    "commands": [
        "LoginUtil.insertLoginRecord"
    ],
    "isReturnOutputs": [
        false
    ],
    "isReturnErrors": [
        false
    ]
}
```
サーバーに参加した際にログイン履歴を記録し、`MCID joined the game`というログをDiscordに送信します