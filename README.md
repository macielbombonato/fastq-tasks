# FastQ Tasks
---------
## 1. Objetivo

O objetivo deste projeto é realizar processos de tratamento em arquivos FastQ com o intuíto de retirar ruídos do arquivo para facilitar sua utilização.

Após executar este aplicativo, no diretório onde o(s) arquivo(s) se encontra(m) aparecerá um novo diretório e neste, as sequências são separadas em arquivos por tipo de processamento, ou seja, sequências pequenas no arquivo de sequências pequenas e assim por diante.

## 2. Funcionalidades

- Remoção de sequências pequenas;
- Remoção de sequências com baixo score de qualidade;
- Remoção de sequências duplicadas;
- Remoção de sequências similares;

### 2.1. Sequências pequenas

São sequências menores que um valor informado pelo usuário no ato de execução do aplicativo.

Todas sequências menores que este valor serão copiadas para seu arquivo respectivo.

### 2.2. Sequências com baixo score de qualidade

Nos arquivos FastQ, cada sequência é representada por quatro linhas seguindo a seguinte lógica:

1. Identificador da sequência;
2. Sequência;
3. Características da sequência. Caiu em desuso e normalmente apresenta apenas o caracter **+**;
4. Linha de qualidade. Cada caracter desta linha representa um score de qualidade do caracter na mesma posição na linha de sequência;

O calculo do score da linha neste aplicativo é feito da seguinte maneira.

O valor ASCII do caracter de qualidade (por exemplo, **A** vale **65**) menos **33** irá representar o score de qualidade do nucleotídeo que estiver na mesma posição.

Caso este valor esteja acima do valor informado pelo usuário na execução da aplicação, ele conta como ponto positivo e caso contrário, conta como ponto negativo.

Ao final, é levantado um percentual da quantidade de pontos positivos e caso o percentual esteja acima do valor informado pelo usuário, esta sequência será aproveitada e será copiada para o arquivo (**RECODE**), caso não, irá para o arquivo com sequências de baixa qualidade (**LOW_QUALITY**).

### 2.3. Sequências duplicadas e similares

Pode acontecer de um fragmento ser copiado mais de uma vez de forma identica e consecutivamente, aparecer mais 
de uma vez no arquivo FastQ. 

Para este caso, temos duas situações.

1. Igualdade, ou seja, a sequência é 100% igual, estes casos são desviados para o arquivo **DUPLICATE**;
2. Similaridade, ou seja, a sequência possui bases de baixa qualidade e estas, quando identificadas são desviadas para o arquivo **SIMILAR**;

O aplicativo detecta uma similaridade quando alguns nucleotídeos são de baixa qualidade (apesar da linha toda ter uma qualidade geral boa) e estes nucleotídeos de baixa qualidade podem na verdade ser a troca de uma base, por exemplo:

A sequência abaixo:

	ACCTGTGAA
	
Gera um score de qualidade abaixo de 20 na terceira posição, ou seja, o **C** pode ser um ruído, sendo assim, este caracter é substituído em um campo de memória (serve apenas para critérios de busca e não deve ser gravado nos arquivos de saída) e a sequência fica conforme abaixo:

	AC_TGTGAA

#### 2.3.1. Identificando duplicidades e similaridades

Esse processo acontece da seguinte forma:

1. O aplicativo gera um banco de dados temporário e carrega nele todas as sequências que inicialmente iriam para o arquivo final do processo;
2. O sistema percorre todas as sequências procurando no banco de dados por similaridades ou duplicidades e caso encontre sequências faz uma segunda análise;
3. Estas sequências selecionadas são avaliadas se a similaridade é de 100% ou aproximada, isso para separar a sequência para o arquivo adequado;


#### 2.3.2. Fórmula de busca

Caso o usuário **não** tenha desativado a análise de similaridades e a sequência possua bases de baixa qualidade o aplicativo irá buscar no banco por sequências parecidas (iguais vem junto) e faz um cruzamento.

	select s
	  from Sequence s
	 where s.id != :id
	   and (
	        ( s.seq1 like :seq1
	      and s.seq2 like :seq2
	        ) 
	     or ( s.seq2 like :seq1
	      and s.seq1 like :seq2
	        )
	       ) 
	    and s.status = :status
	    
Caso o usuário tenha desativado a análise de similaridade, a busca tem uma pequena alteração.

	select s
	  from Sequence s
	 where s.id != :id
	   and (
	        ( s.seq1 = :seq1
	      and s.seq2 = :seq2
	        ) 
	     or ( s.seq2 = :seq1
	      and s.seq1 = :seq2
	        )
	       ) 
	    and s.status = :status
	    
Os campos buscados na pesquisa são os seguintes:

1. **seq1 --> Sequência do primeiro arquivo;
2. seq2 --> Sequência par da primeira, porém, localizada no segundo arquivo;
3. status --> Conforme similaridades e duplicidades são encontradas, estas são marcadas, então, para que uma sequência não seja analisada duas vezes, o sistema marca as que devem ser utilizadas e as que já constam para "descarte";

**OBS.**: Caso tenha sido informado apenas um arquivo, desconsidere a sequência dois a a busca cruzada.

### 2.3.3. Resultado

O aplicativo após todo este processo de busca, irá gerar um, dois ou três arquivos. Cada um com seu tipo respectivo de sequência.

## 3. Utilização

### 3.1. Parâmetros

	r1=/path/to/yourfile.1.fastq
	
Primeiro (ou único) arquivo **fastq**. Este parâmetro é mandatório.

	r2=/path/to/yourfile.2.fastq
	
Segundo arquivo **fastq** de uma leitura pariada.

	buffer=10000
	
Quantidade de registros que podem ser carregados para a memória.

O objetivo deste parâmetro é (sempre que possível) armazenar uma quantidade de registros em memória antes de processá-los e enviá-los para seus respectivos arquivos. 

Deve ser ajustado conforme a capacidade de cada máquina.

Valor padrão = 100. Recomendado = 10000.

	seq=100
	
Tamanho mínimo de uma sequência para que seja enviada para o arquivo final.

As sequências menores que este tamanho serão enviadas para o arquivo **SMAL_SEQ** e serão desconsideradas em todos os outros processamentos.

Valor padrão = 80

	qual=20
	
Qualidade mínima de cada base. 

Este valor será utilizado para validar se uma base deve ser considerada como positiva ou negativa durante o cálculo de score da sequência.

Valor padrão = 20

	qualPerc=80
	
Percentual mínimo de qualidade de uma sequência.

Sequências com score abaixo deste valor, serão designadas automaticamente para o arquivo **LOW_QUALITY**.

OBS.: Quando a sequência possuir um bom valor de qualidade, porém, possuir bases de baixa qualidade, estas serão trocadas pelo caracter "coringa" para que seja realizada a busca por similaridade (caso não tenha sido desativada).

Valor padrão = 80

	--help
	
Exibe a lista de parâmetros disponíveis do aplicativo e como utilizá-los.

	--skip-duplicates
	
Desativa a análise de duplicidade e similaridade.

Este parâmetro faz com que o processo de tratamento dos arquivos seja de fato muito mais rápido, portanto, deve-se considerar o uso dele quando há pouco tempo para realizar o tratamento de um arquivo.

	--skip-similar
	
Caso o usuário queira uma análise de duplicidade, mas não queira a análise de similaridade, basta utilizar este parâmetro.

	--skip-size
	
Desativa a análise de tamanho da sequência e a criação do arquivo **SMALL_SEQ**.

	--skip-quality
	
Desativa a análise de qualidade da sequência e a criação do arquivo **LOW_QUALITY**.

	--skip-progress-bar
	
Desativa a exibição da barra de progresso no terminal.

Muito útil para máquinas com baixa performance.

### 3.2. Requisitos

Se você é apenas usuário, basta ter o Java 1.6+ instalado em sua máquina. 

Recomendamos a versão 1.8+.

Desenvolvedores devem possuir o Java (JDK) seguindo o mesmo padrão para versões.

É necessário também que o desenvolvedor possua o Maven 3.2+ instalado.

### 3.3. Download do fonte e empacotamento

Para baixar o código fonte para sua máquina, basta executar o comando:

	$ git clone https://bitbucket.org/unesp/fastq-tasks.git
	
Para gerar o jar que será distribuído execute o seguinte comando na pasta raiz do projeto:

	$ mvn clean install
	
Feito isso, o jar gerado estará dentro da pasta **target**. Este é o executável que deve ser distribuído.

### 3.3. Download do executável

Baixe a última versão [clicando aqui](https://bitbucket.org/unesp/fastq-tasks/downloads/fastq-tasks-1.1.0.jar).

Ou acesse a área de download com todas versões geradas [clicando aqui](https://bitbucket.org/unesp/fastq-tasks/downloads).

### 3.4. Execução

No terminal do seu computador execute o comando:

	$ java -jar fastq-tasks-[versao].jar r1=/path/fastqfile.fastq buffer=10000
	
