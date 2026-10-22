# ArquivIndex

Sistema de catalogação de acervos digitais em Java (Swing + JDBC + MySQL),
projeto da disciplina de Programação Orientada a Objetos. Ver
`ESPECIFICACAO_ArquivIndex.md` para o detalhamento completo do projeto
(escopo, modelo de dados, decisões de arquitetura).

Este README é o passo a passo para colocar o projeto para rodar numa
máquina que nunca viu o código antes.

## 1. Pré-requisitos

- **JDK 21** ou superior instalado (`java -version` para conferir).
- **MySQL 8** (ou compatível) instalado e em execução.
- Não é necessário ter o Maven instalado à parte — o projeto usa o
  **Maven Wrapper** (`mvnw`/`mvnw.cmd`), que baixa o Maven sozinho na
  primeira execução, usando apenas o JDK.

## 2. Criar o banco de dados

Rode o script `db/setup.sql` num MySQL local, com um usuário que tenha
permissão de criar bancos e usuários (normalmente `root`):

```bash
mysql -u root -p < db/setup.sql
```

O script cria o banco `arquivindex`, o usuário de aplicação
`arquivindex_app` (senha `senha123`) e as cinco tabelas do modelo
(`colecao`, `categoria`, `arquivo`, `arquivo_categoria`,
`historico_alteracao`) com os relacionamentos e índices.

Se o usuário `arquivindex_app` já existir num banco de outra pessoa (ou o
comando `CREATE USER` falhar por já existir), pode rodar só a parte de
`CREATE TABLE`/`CREATE INDEX` do script manualmente.

## 3. Ajustar as credenciais (se precisar)

As credenciais do banco **não estão fixas no código** — ficam em
`src/main/resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/arquivindex?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.usuario=arquivindex_app
db.senha=senha123
```

Se o MySQL estiver em outra máquina, outra porta, ou se você criou o
usuário/senha do jeito que preferir no passo 2, edite este arquivo — não
precisa mexer em nenhum `.java` nem recompilar nada além de rodar o
programa de novo (o Maven copia o `.properties` para dentro do build
automaticamente).

Se o arquivo estiver ausente ou faltando alguma das três chaves, o
programa avisa exatamente qual chave falta em vez de travar sem explicação.

## 4. Executar

No Windows:

```
mvnw.cmd exec:java
```

No Linux/macOS:

```bash
./mvnw exec:java
```

Na primeira execução, o wrapper baixa o Maven automaticamente (precisa de
internet nessa primeira vez); das próximas em diante ele reusa a cópia já
baixada. Em seguida a janela do ArquivIndex abre normalmente.

## 5. Solução de problemas

**"Não foi possível conectar ao banco de dados. Verifique se o MySQL está
em execução."**
O MySQL não está rodando, ou a URL em `config.properties` aponta para
host/porta errados. Confirme que o serviço do MySQL está ativo
(`Get-Service *mysql*` no Windows, `systemctl status mysql` no Linux) e que
a porta em `db.url` bate com a porta real do servidor (padrão: 3306).

**"Usuário ou senha do banco de dados estão incorretos."**
`db.usuario`/`db.senha` em `config.properties` não batem com o que foi
criado no passo 2. Reconfira as credenciais, ou rode de novo o trecho
`CREATE USER`/`GRANT` do `db/setup.sql`.

**Erro de autenticação do MySQL 8 (`Public Key Retrieval is not allowed` ou
similar)**
O MySQL 8 usa por padrão o plugin de autenticação `caching_sha2_password`,
mais rigoroso que versões antigas. A URL já inclui
`allowPublicKeyRetrieval=true` para lidar com isso; se ainda assim der
erro, confirme que essa opção não foi removida de `db.url` em
`config.properties`.

**Acentuação aparece quebrada no terminal (ex.: `Descri????o`)**
Isso é o terminal exibindo errado, não um problema do programa — os dados
são gravados em UTF-8 (`utf8mb4`) corretamente no banco (dá para conferir
rodando uma consulta direto no `mysql` com
`SET NAMES utf8mb4;` antes do `SELECT`). No Windows, rodar
`chcp 65001` no `cmd`/PowerShell antes de usar o cliente `mysql` costuma
resolver a exibição no terminal. Na interface gráfica do ArquivIndex
(Swing) a acentuação aparece correta normalmente, já que não depende do
console.

**Janela do programa não aparece e não dá nenhum erro visível**
Confira se há algum diálogo de erro atrás de outras janelas — erros de
configuração ou de conexão são mostrados num `JOptionPane` assim que o
programa tenta abrir, mas a conexão com o banco pode levar alguns segundos
para falhar (timeout de rede) antes do diálogo aparecer.
