# ArquivIndex — Especificação do Projeto

---

## 0. Histórico e motivo desta versão

A proposta inicial foi **aprovada** pelo professor como tecnicamente viável,
com duas ressalvas:

1. **Escopo simples demais para uma dupla** — concentrava-se em cadastro,
   listagem e ordenação de *uma única entidade*, sem relacionamentos, o que
   limita a demonstração de POO.
2. **Submissão contraditória** mencionando menu em terminal — desconsiderada.
   A interface gráfica (GUI) é obrigatória.

Orientação recebida: ampliar o modelo com **Categoria/Tag**, **Coleção/Pasta
lógica**, **Localização/Caminho**, **Favoritos** e **histórico de alterações**,
além de **buscas combinadas** e regras de organização.

Esta v2 incorpora os cinco itens. **Consequência importante:** como o professor
exige histórico de alterações, a funcionalidade de **editar registros volta ao
escopo** (não há histórico sem alteração), e os campos editáveis do Model
deixam de ser `final`.

---

## 1. Visão geral

**ArquivIndex** é um sistema de catalogação de acervos digitais. O sistema não
varre o disco: o usuário **cadastra manualmente** os metadados dos arquivos,
organiza-os em coleções, classifica-os com categorias e mantém o registro de
todas as alterações feitas.

**Problema resolvido:** desorganização de acervos digitais pessoais ou de
pequenos escritórios, onde arquivos acumulados carecem de registro estruturado
que permita localizá-los, classificá-los e rastrear seu histórico.

---

## 2. Requisitos obrigatórios da disciplina

Todos devem estar contemplados no projeto final:

- Conteúdo base de Java visto no semestre
- **Tratamento de exceções** em todos os pontos de falha possíveis
- **GUI (Graphical User Interfaces)** em Swing ou JavaFX — decisão: **Swing**
- Arquitetura **MVC (Model-View-Controller)**
- Integração com banco de dados via **JDBC (Java Database Connectivity)**
- Padrão de projeto **Singleton** para a conexão com o banco
- Padrão de projeto **DAO (Data Access Object)** **usando interfaces**
- **Manipulação correta de datas**, tanto no SGBD quanto no Java

**SGBD (Sistema de Gerenciamento de Banco de Dados):** MySQL 8.

---

## 3. Escopo

### Dentro do escopo

**Arquivos**
- Cadastrar, editar e listar arquivos
- Campos do usuário: nome, tipo, descrição, caminho/localização
- Campos automáticos: ID, data de criação, data de última modificação
- Marcar/desmarcar como **favorito**

**Coleções (pastas lógicas)**
- Cadastrar e listar coleções
- Associar um arquivo a uma coleção (relacionamento 1:N)

**Categorias (tags)**
- Cadastrar e listar categorias
- Associar várias categorias a um arquivo e vice-versa (relacionamento N:N)

**Histórico de alterações**
- Registro automático de criação e de cada edição
- Guarda: campo alterado, valor anterior, valor novo, tipo de operação e data
- Consulta do histórico de um arquivo

**Consulta**
- **Ordenação** por nome, tipo e data de modificação — crescente e decrescente
- **Busca combinada**: filtrar simultaneamente por texto (nome/descrição),
  tipo, coleção, categoria e favoritos

### Fora do escopo (não implementar)
- Varredura automática do sistema de arquivos real
- Upload ou armazenamento do conteúdo dos arquivos (apenas metadados)
- Suporte multiusuário, em rede ou autenticação/login
- Menu em terminal (**explicitamente desconsiderado pelo professor**)

---

## 4. Modelo de dados

### 4.1 Entidades e relacionamentos

```
    COLECAO 1 ─────── N ARQUIVO N ─────── N CATEGORIA
                           │          (arquivo_categoria)
                           │ 1
                           │
                           N
                  HISTORICO_ALTERACAO
```

- **Coleção → Arquivo (1:N)** — uma coleção contém vários arquivos; cada
  arquivo pertence a no máximo uma coleção (pode não ter nenhuma).
- **Arquivo ↔ Categoria (N:N)** — um arquivo pode ter várias categorias e uma
  categoria pode marcar vários arquivos. Exige tabela de junção.
- **Arquivo → Histórico (1:N)** — cada arquivo acumula vários registros de
  alteração ao longo do tempo.

### 4.2 Campos

**arquivo**

| Campo | Origem | Tipo Java | Tipo MySQL |
|---|---|---|---|
| id | automático (banco) | `int` | `INT AUTO_INCREMENT PRIMARY KEY` |
| nome | usuário | `String` | `VARCHAR(150) NOT NULL` |
| tipo | usuário | `String` | `VARCHAR(50) NOT NULL` |
| descricao | usuário | `String` | `VARCHAR(500)` |
| caminho | usuário | `String` | `VARCHAR(400)` |
| favorito | usuário | `boolean` | `BOOLEAN NOT NULL DEFAULT FALSE` |
| dataCriacao | automático | `LocalDateTime` | `DATETIME NOT NULL` |
| dataModificacao | automático | `LocalDateTime` | `DATETIME NOT NULL` |
| colecaoId | usuário (seleção) | `Integer` | `INT` (FK, aceita nulo) |

**colecao**

| Campo | Tipo Java | Tipo MySQL |
|---|---|---|
| id | `int` | `INT AUTO_INCREMENT PRIMARY KEY` |
| nome | `String` | `VARCHAR(100) NOT NULL UNIQUE` |
| descricao | `String` | `VARCHAR(300)` |
| dataCriacao | `LocalDateTime` | `DATETIME NOT NULL` |

**categoria**

| Campo | Tipo Java | Tipo MySQL |
|---|---|---|
| id | `int` | `INT AUTO_INCREMENT PRIMARY KEY` |
| nome | `String` | `VARCHAR(60) NOT NULL UNIQUE` |
| cor | `String` | `VARCHAR(7)` (hexadecimal, ex.: `#EF4444`) |

**historico_alteracao**

| Campo | Tipo Java | Tipo MySQL |
|---|---|---|
| id | `int` | `INT AUTO_INCREMENT PRIMARY KEY` |
| arquivoId | `int` | `INT NOT NULL` (FK) |
| campoAlterado | `String` | `VARCHAR(50) NOT NULL` |
| valorAnterior | `String` | `VARCHAR(500)` |
| valorNovo | `String` | `VARCHAR(500)` |
| tipoOperacao | `enum` | `VARCHAR(20) NOT NULL` |
| dataAlteracao | `LocalDateTime` | `DATETIME NOT NULL` |

---

## 5. Script do banco (validado)

> Este script **foi executado e testado** em MySQL 8: criação, inserções,
> relacionamentos N:N, e as três regras de integridade (ver §5.1).

```sql
CREATE DATABASE IF NOT EXISTS arquivindex
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'arquivindex_app'@'localhost' IDENTIFIED BY 'senha123';
GRANT ALL PRIVILEGES ON arquivindex.* TO 'arquivindex_app'@'localhost';
FLUSH PRIVILEGES;

USE arquivindex;

-- COLECAO — pasta lógica que agrupa arquivos (1:N com arquivo)
CREATE TABLE IF NOT EXISTS colecao (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL UNIQUE,
    descricao     VARCHAR(300),
    data_criacao  DATETIME NOT NULL
);

-- CATEGORIA — etiqueta/tag aplicável a vários arquivos (N:N)
CREATE TABLE IF NOT EXISTS categoria (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(60) NOT NULL UNIQUE,
    cor   VARCHAR(7)
);

-- ARQUIVO — entidade principal do catálogo
CREATE TABLE IF NOT EXISTS arquivo (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(150) NOT NULL,
    tipo              VARCHAR(50)  NOT NULL,
    descricao         VARCHAR(500),
    caminho           VARCHAR(400),
    favorito          BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao      DATETIME NOT NULL,
    data_modificacao  DATETIME NOT NULL,
    colecao_id        INT,

    CONSTRAINT fk_arquivo_colecao
        FOREIGN KEY (colecao_id) REFERENCES colecao(id)
        ON DELETE SET NULL
);

-- ARQUIVO_CATEGORIA — tabela de junção do relacionamento N:N
CREATE TABLE IF NOT EXISTS arquivo_categoria (
    arquivo_id    INT NOT NULL,
    categoria_id  INT NOT NULL,

    PRIMARY KEY (arquivo_id, categoria_id),

    CONSTRAINT fk_ac_arquivo
        FOREIGN KEY (arquivo_id) REFERENCES arquivo(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ac_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id)
        ON DELETE CASCADE
);

-- HISTORICO_ALTERACAO — trilha de auditoria (1:N com arquivo)
CREATE TABLE IF NOT EXISTS historico_alteracao (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    arquivo_id      INT NOT NULL,
    campo_alterado  VARCHAR(50)  NOT NULL,
    valor_anterior  VARCHAR(500),
    valor_novo      VARCHAR(500),
    tipo_operacao   VARCHAR(20)  NOT NULL,
    data_alteracao  DATETIME     NOT NULL,

    CONSTRAINT fk_historico_arquivo
        FOREIGN KEY (arquivo_id) REFERENCES arquivo(id)
        ON DELETE CASCADE
);

-- Índices para acelerar buscas e ordenações frequentes
CREATE INDEX idx_arquivo_nome      ON arquivo(nome);
CREATE INDEX idx_arquivo_tipo      ON arquivo(tipo);
CREATE INDEX idx_arquivo_data_mod  ON arquivo(data_modificacao);
CREATE INDEX idx_historico_data    ON historico_alteracao(data_alteracao);
```

### 5.1 Regras de integridade (testadas, valem como ponto de apresentação)

| Ação | Comportamento | Mecanismo |
|---|---|---|
| Apagar uma **coleção** | Os arquivos **sobrevivem**, com `colecao_id` = NULL | `ON DELETE SET NULL` |
| Apagar um **arquivo** | Vínculos com categorias e histórico somem junto | `ON DELETE CASCADE` |
| Repetir a mesma categoria no mesmo arquivo | Rejeitado pelo banco | Chave primária composta |

---

## 6. Estrutura de pastas (Maven)

```
src/main/java/
├── model/
│   ├── Arquivo.java
│   ├── Colecao.java
│   ├── Categoria.java
│   ├── HistoricoAlteracao.java
│   └── TipoOperacao.java        (enum: CRIACAO, EDICAO)
├── dao/
│   ├── ConexaoBD.java           (Singleton)
│   ├── ArquivoDAO.java          (interface)
│   ├── ArquivoDAOImpl.java
│   ├── ColecaoDAO.java          (interface)
│   ├── ColecaoDAOImpl.java
│   ├── CategoriaDAO.java        (interface)
│   ├── CategoriaDAOImpl.java
│   ├── HistoricoDAO.java        (interface)
│   └── HistoricoDAOImpl.java
├── controller/
│   ├── ArquivoController.java
│   ├── ColecaoController.java
│   ├── CategoriaController.java
│   ├── CriterioOrdenacao.java   (enum: NOME, TIPO, DATA)
│   ├── FiltroBusca.java         (objeto com os critérios da busca combinada)
│   └── ArquivoException.java    (exceção de negócio)
└── view/
    ├── JanelaPrincipal.java
    ├── PainelArquivos.java
    ├── PainelColecoes.java
    ├── PainelCategorias.java
    └── DialogoHistorico.java
```

---

## 7. Especificação de implementação

### 7.1 Model

Classes simples (POJOs) representando as entidades. Pontos de atenção:

- **`Arquivo`**: `id` mutável (o banco atribui após o `INSERT`, via
  `setId()`). Campos editáveis (nome, tipo, descrição, caminho, favorito,
  coleção) **não** são `final`, pois há edição. `dataCriacao` é `final`;
  `dataModificacao` é atualizada a cada edição.
- **`Arquivo`** mantém também `List<Categoria> categorias`, preenchida pelo DAO.
- **`TipoOperacao`**: `enum` com `CRIACAO` e `EDICAO`.
- Todas as classes sobrescrevem `toString()` de forma legível.

### 7.2 DAO

**Uma interface + uma implementação por entidade** (requisito explícito da
disciplina: "utilizando Interfaces").

Métodos mínimos:

- `ArquivoDAO`: `inserir`, `atualizar`, `listarTodos`, `buscarPorId`,
  `buscar(FiltroBusca)`, `vincularCategoria`, `desvincularCategoria`,
  `listarCategoriasDoArquivo`
- `ColecaoDAO`: `inserir`, `listarTodas`, `buscarPorId`
- `CategoriaDAO`: `inserir`, `listarTodas`, `buscarPorId`
- `HistoricoDAO`: `inserir`, `listarPorArquivo`

Regras técnicas obrigatórias:

- Sempre `PreparedStatement` (nunca concatenar SQL — evita injeção de SQL)
- `Statement.RETURN_GENERATED_KEYS` para recuperar o ID gerado
- `try-with-resources` para fechar `PreparedStatement` e `ResultSet`
- Conversão de datas: `Timestamp.valueOf(localDateTime)` na ida,
  `rs.getTimestamp(...).toLocalDateTime()` na volta
- Métodos declaram `throws SQLException` — o DAO **não** engole erro

**Busca combinada:** montar o SQL dinamicamente conforme os filtros preenchidos
em `FiltroBusca`, acrescentando cláusulas e parâmetros ao `PreparedStatement`.
Filtro por categoria exige `JOIN` com `arquivo_categoria`.

**Transação no vínculo N:N:** ao salvar um arquivo com suas categorias, usar
`conexao.setAutoCommit(false)`, executar o `INSERT` do arquivo e os vínculos, e
então `commit()`; em caso de erro, `rollback()`. Isso evita arquivo salvo com
categorias pela metade.

### 7.3 Controller

Ponte entre View e DAO. Responsabilidades:

- **Validar entradas**: nome e tipo obrigatórios (não vazios, não só espaços);
  respeitar os limites de tamanho das colunas (150/50/500/400/100/60)
- **Registrar histórico automaticamente**: ao criar, gravar um registro
  `CRIACAO`; ao editar, comparar o objeto antigo com o novo e gravar **um
  registro por campo alterado**, com valor anterior e novo
- **Atualizar `dataModificacao`** a cada edição
- **Ordenar** com `Comparator` (ver §7.4)
- **Traduzir exceções**: capturar `SQLException` e lançar `ArquivoException`
  com mensagem compreensível. Detectar falha de conexão (`SQLState` iniciado em
  `08`) e emitir mensagem específica de banco indisponível. A View nunca deve
  ver uma `SQLException` crua.

### 7.4 Ordenação — decisão de arquitetura

A ordenação fica **no Controller, com `Comparator`**, e **não** via `ORDER BY`
no SQL. Justificativa (ponto de apresentação): o DAO tem a responsabilidade
única de acessar dados; ordenar é regra de apresentação. Isso preserva a
separação de responsabilidades do MVC.

- Por **nome** (alfabética, case-insensitive), por **tipo**, por **data de
  modificação**
- Cada um em ordem crescente e decrescente (`.reversed()`)

> Observação: a **filtragem** (busca combinada) fica no SQL, pois filtrar no
> banco evita trazer registros desnecessários para a memória. Ordenar depois,
> em memória, é barato. Essa distinção entre *filtrar no banco* e *ordenar na
> aplicação* é uma boa resposta de apresentação.

### 7.5 View (Swing, codado à mão)

**Sem construtor visual** — os autores precisam explicar cada linha; código
gerado por ferramenta é difícil de defender.

- `JanelaPrincipal` com `JTabbedPane`: abas Arquivos, Coleções, Categorias
- **Aba Arquivos**: formulário de cadastro/edição (`JTextField` para nome,
  tipo, caminho; `JTextArea` para descrição; `JComboBox` para coleção;
  `JCheckBox` para favorito; lista de seleção múltipla para categorias),
  `JTable` não editável com os registros, botões Cadastrar / Salvar edição /
  Ver histórico
- **Painel de busca**: campo de texto, combos de tipo/coleção/categoria,
  checkbox "somente favoritos", botão Buscar e botão Limpar filtros
- **Controles de ordenação**: `JComboBox` de critério + `JRadioButton`
  crescente/decrescente
- **`DialogoHistorico`**: `JDialog` com `JTable` do histórico do arquivo
  selecionado
- `JOptionPane` para mensagens de sucesso e erro
- Datas exibidas como `dd/MM/yyyy HH:mm:ss` via `DateTimeFormatter` — nunca o
  `toString()` cru do `LocalDateTime`

### 7.6 Tratamento de exceções

Cobrir no mínimo:

- Campos obrigatórios vazios
- Texto excedendo o tamanho da coluna
- Nome de coleção ou categoria duplicado (violação de `UNIQUE`)
- Vínculo de categoria repetido (violação de chave composta)
- Falha de conexão / banco fora do ar
- Nenhum registro selecionado ao clicar em editar ou ver histórico

Fluxo: DAO declara `throws SQLException` → Controller captura e converte em
`ArquivoException` → View exibe via `JOptionPane` sem quebrar a aplicação.

### 7.7 Classe principal

`Main.java` inicia a GUI dentro de `SwingUtilities.invokeLater(...)`.

---

## 8. Convenções e advertências

- **Idioma:** código, comentários e mensagens em **português**, incluindo nomes
  de classes e métodos (`Arquivo`, `inserir`, `listarTodos`)
- **Encoding:** UTF-8 em tudo; compilar com `-encoding UTF-8`. Acentuação
  quebrada ("Descri??o") indica encoding mal configurado
- **Sem frameworks:** nada de Spring, Hibernate ou Lombok. JDBC puro e Swing
  puro — o projeto precisa demonstrar os padrões implementados manualmente
- **Comentários** explicam o *porquê*, não o *quê*
- **Simplicidade acima de esperteza:** preferir código direto e legível
- **Não inventar funcionalidades** fora da §3

---

## 9. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.arquivindex</groupId>
    <artifactId>arquivindex</artifactId>
    <version>1.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>26.7.0</version>
        </dependency>
    </dependencies>
</project>
```

Conexão (usar em `ConexaoBD`):

```
jdbc:mysql://localhost:3306/arquivindex?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

---

## 10. Contexto adicional

Após a entrega em Java, **parte deste programa será convertida para C** em
outro projeto. Isso favorece manter a lógica clara e pouco dependente de
recursos exclusivos de Java onde possível — as classes de modelo mapeiam bem
para `struct`.

**Prazo:** aproximadamente 1 mês. **Equipe:** dupla.

**Plano de referência por semana:**
1. Model completo + banco + os quatro DAOs
2. Controllers, histórico automático, busca combinada, ordenação, exceções
3. Interface gráfica (abas, busca, histórico)
4. Casos extremos, ajustes e preparação da apresentação

---

## 11. Instruções de execução (Claude Code)

### 11.1 Iniciar a sessão

Rodar no terminal, dentro da pasta do projeto:

```bash
claude --dangerously-skip-permissions
```

Essa flag desativa a confirmação manual a cada ação (criar arquivo, rodar
comando, instalar dependência), permitindo que o agente trabalhe sem
interrupção. **Só usar em pasta de projeto isolada** — sem a confirmação, o
agente pode alterar ou apagar qualquer arquivo ao alcance dele. Nunca rodar na
pasta pessoal ou na raiz do sistema.

### 11.2 Prompt inicial

```
Leia ESPECIFICACAO_ArquivIndex.md por completo antes de qualquer ação.

Esta é a v2 da especificação: o escopo foi ampliado a pedido do professor.
Se já existir código da v1 no projeto, ele precisa ser adaptado — o modelo
agora tem quatro entidades e relacionamentos, e a edição de registros voltou
ao escopo.

Implemente conforme a especificação, respeitando:
- o escopo da §3 (não implementar nada fora dele)
- o modelo de dados e o script validado da §4 e §5
- a estrutura de pastas da §6
- as decisões de arquitetura da §7 (DAO com interfaces, ordenação no
  Controller, filtragem no SQL, Swing à mão, histórico automático)
- as convenções da §8 (português, UTF-8, sem frameworks)

Requisito central: os autores precisam conseguir explicar cada linha na
apresentação. Prefira código direto e legível a soluções compactas.

Ao final, compile e execute o projeto para verificar que funciona de fato.
Se faltar JDK (Java Development Kit) ou Maven no ambiente, instale antes de
testar. Não declare nada como concluído sem ter compilado e rodado.

Execute também as tarefas de portabilidade da §12 (configuração externa,
Maven Wrapper, README e o teste de banco indisponível).

Depois, gere o relatório descrito na §11.3.
```

### 11.3 Relatório obrigatório ao final

O agente deve produzir um relatório contendo:

1. **O que foi feito** — arquivos criados ou alterados, e o que cada um faz.
2. **Verificação real** — o projeto compilou? Executou? Quais comandos foram
   usados e qual foi a saída? **Se não foi possível compilar, dizer isso com
   todas as letras e não afirmar que está pronto.**
3. **Testes executados** — o que foi testado de fato (cadastro, edição,
   histórico automático, vínculo N:N, busca combinada, ordenação, campos
   inválidos, banco fora do ar) e o resultado de cada um.
4. **Desvios da especificação** — qualquer coisa implementada de forma
   diferente do especificado, com justificativa.
5. **Pendências e riscos** — o que ficou por fazer, o que pode quebrar, o que
   depende de configuração da máquina dos autores.
6. **Pontos que os autores precisam estudar** — trechos cuja lógica exige
   atenção para conseguir explicar na apresentação (especialmente: Singleton,
   transação do vínculo N:N, montagem dinâmica do SQL da busca, geração
   automática do histórico).

## 12. Portabilidade (obrigatório antes da entrega)

**Problema:** hoje o projeto só roda na máquina onde foi desenvolvido. Usuário,
senha e URL do banco estão fixos no código-fonte, e a execução depende de
`JAVA_HOME` e Maven configurados no `PATH` do sistema. Se o professor rodar em
outro computador — ou se a apresentação for num laboratório — o programa falha
na conexão e exigiria editar o código e recompilar.

Implementar as quatro tarefas abaixo:

### 12.1 Externalizar a configuração do banco

Criar `src/main/resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/arquivindex?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.usuario=arquivindex_app
db.senha=senha123
```

`ConexaoBD` deve ler esse arquivo (via `Properties` e
`getClass().getClassLoader().getResourceAsStream("config.properties")`) em vez
de ter as credenciais escritas no código. Se o arquivo não for encontrado ou
estiver incompleto, lançar exceção com mensagem clara indicando o que falta —
não falhar silenciosamente.

Assim, quem receber o projeto ajusta um arquivo de texto, sem recompilar nada.

### 12.2 Adicionar o Maven Wrapper

Gerar o wrapper no projeto (`mvn wrapper:wrapper`), de modo que passem a existir
`mvnw`, `mvnw.cmd` e a pasta `.mvn/`. Com isso, quem receber o projeto precisa
apenas do JDK (Java Development Kit) instalado — o Maven é baixado
automaticamente na primeira execução, sem depender de `PATH`.

Comandos passam a ser `./mvnw` (Linux/macOS) ou `mvnw.cmd` (Windows).

### 12.3 Escrever o README.md

Na raiz do projeto, com passo a passo para alguém que nunca viu o sistema:

1. Pré-requisitos (JDK 21+, MySQL 8)
2. Como criar o banco (`mysql -u root -p < db/setup.sql`)
3. Como ajustar `config.properties` caso as credenciais locais sejam outras
4. Como executar (`mvnw.cmd exec:java` / `./mvnw exec:java`)
5. Seção de solução de problemas: erro de conexão, erro de autenticação do
   MySQL 8, acentuação no terminal

### 12.4 Testar o tratamento de "banco fora do ar"

Este cenário ficou sem teste real. **Não é necessário derrubar o serviço
MySQL:** basta alterar temporariamente a senha (ou a porta) em
`config.properties` para um valor inválido e executar o programa. A falha de
conexão ocorre da mesma forma, e permite verificar se a mensagem exibida ao
usuário é compreensível. Restaurar a configuração correta ao final e relatar o
resultado.

### 12.5 Limpeza

Verificar se a tabela `arquivos` (plural, resíduo da v1) está vazia
(`SELECT COUNT(*) FROM arquivos;`). Se estiver, removê-la com
`DROP TABLE arquivos;`. Se tiver qualquer linha, **não apagar** — avisar o autor.

---

