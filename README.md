# プロジェクト

## 目標
通信モデルを作って暗号の安全性の検証をする．


# ドキュメント

## project
ルートパッケージ

## project.lib
ライブラリの定義を入れるパッケージ．

このパッケージ直下のクラスは暗号，プロトコル共通で用いられる

### project.lib.DuplexStream
双方向通信用のストリームを表すインターフェイス

#### メンバー
##### read
定義

```java
public int read(byte[] destination);
```
データを受信してdestinationに書き込み，書き込んだバイト数を返す．ただし，最後のブロックを書き込んだ場合は書き込んだバイト数のnotを返す．

スレッドセーフである必要がある．


##### write
```java
public void write(byte[] source, int length);
```
sourceからlengthバイトを送信する．lengthが負のときは最後のブロックであることを表す．

スレッドセーフである必要がある．

## project.lib.crypto
暗号の定義を入れるパッケージ

## project.lib.protocol
プロトコルの定義を入れるパッケージ

### project.lib.protocol.MetaMessage
```ebnf
MetaMessage ::= Id, "@", MessageBody, "\0";
         Id ::= "[_a-zA-Z]+";
    RuleSet ::= ;
    RecRule ::= Id, ':', RuleSet, ';';
       Rule ::= Id, '=', Value;
      Value ::= Atom | Atom, (',', Atom)*, [','];
      Atom ::= '"', Value, '"' | "'", Value, "'" | ".*";
       
```
サンプル: `key0:key01=v01&key02:key001=v001&key002=""aa"a"";;&key1=v1`
これが表すデータは
```json
{ 
    "key0": { 
        "key01":"v01",
        "key02": { 
            "key001": "v001",
            "key002": "aa\"a",
        },
    }
    "key1": "v1", 
}
```

## project.test
エントリポイントなど実行にかかわるものを入れるパッケージ
