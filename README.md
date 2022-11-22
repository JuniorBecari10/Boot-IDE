# Boot IDE

## English

Boot IDE is a code editor purely made in Java. <br />
If you like pixels and programming, this is the right option!

### How to Run

Go to the Releases page on GitHub and select the latest. <br />
There are 3 compiled forms: **EXE**, **JAR** and **Linux Executable**. Choose the option that more suits you.

### How to Compile

**If you want to compile from source, do this:** <br />
First, download the code in your computer via **git clone** or the **Code** button on GitHub.

#### By an IDE

Open the project on your favorite IDE and click Run/Execute.

#### by the Command-Line (CLI)

Boot IDE doesn't have dependencies, so it's easy to compile and run. Follow the steps below: <br />

##### To Compile

You can use `make`, `make build` or `make compile` to compile (requires make). It will run the following command:

```batch
cd src
javac -J-Dfile.encoding=UTF-8 -d ../bin ide/main/Start.java
```

##### To Run

You can use `make run` to run (requires make). It will run the following command:

```batch
cd bin
java ide.main.Start -cp .
```

##### To Compile and Run

You can use `make compile run` or `make build run` to compile and run (requires make). It will run the following commands:

```batch
cd src
javac -d ../bin ide/main/Start.java

cd ../bin
java -Dfile.encoding=UTF-8 ide.main.Start -cp .
```

### How to generate a JAR file

To generate a JAR file, you need to have the compiled program (i.e. the .class files), a manifest file (look below how to create one), then execute:

You can use `make jar` to generate (requires make). It will run the following command:

_The command `make createjar` also works, but you need to run `make manifest` first and `make clean` or delete the `manifest.txt` file._

```batch
jar cfm Boot-IDE.jar ../manifest.txt *
```

### How to generate a manifest file

To generate a JAR file, you need to have the compiled program (i.e. the .class files) and a manifest file. Here's how to create one:

You can use `make manifest` too (requires make). It will do the following steps: 

- Create a .txt file in base folder;
- Write this line in it:

```
Main-Class: ide.main.Start
```

### How to run a JAR file

Once generated the JAR _(see How to generate a JAR file)_, you can run it by executing:

You can use `make launchjar` to run (requires make). It will run the following command:

```batch
java -jar Boot-IDE.jar
```

### How to generate a Windows executable file

Once generated the JAR _(see How to generate a JAR file)_, you can generate it by executing:

You can use `make exe` to run (requires make and Launch4J). It will run the following command:

```batch
launch4jc exe-template.xml
```

### How to Use

In the first time you open Boot IDE, it doesn't have any folders loaded. Then click on **Select Base Folder** button, and select one. <br />
Then, click the file you want to edit, and start coding!

## Português

Boot IDE é um editor de código puramente feito em Java.
Se você curte pixels e programação, essa é a opção certa!

### Como Executar

Vá para a página Releases no GitHub e selecione a última. <br />
Existem 3 tipos compilados: **EXE**, **JAR** e **Linux Executable**. Escolha a opção que mais se adequa a você.

### Como Compilar

**Se você quer compilar do código-fonte, faça isso:** <br />
Primeiro, baixe o código no seu computador via **git clone** ou o botão **Code** no GitHub.

#### Por uma IDE

Abra o projeto na sua IDE preferida e clique em Executar.

#### Pela Linha de Comando (CLI)

A Boot IDE não tem dependências, então é fácil compilar e executar. Siga os passos abaixo: <br />

##### Para Compilar

Você pode usar `make`, `make build` ou `make compile` para compilar (requer make instalado). Ele vai executar o seguinte comando:

```batch
cd src
javac -J-Dfile.encoding=UTF-8 -d ../bin ide/main/Start.java
```

##### Para Executar

Você pode usar `make run` para executar (requer make instalado). Ele vai executar o seguinte comando:

```batch
cd bin
java -Dfile.encoding=UTF-8 ide.main.Start -cp .
```

##### Para Compilar e Executar

Você pode usar `make compile run` ou `make build run` para compilar e executar (requer make instalado). Ele vai executar os seguintes comandos:

```batch
cd src
javac -d ../bin ide/main/Start.java

cd ../bin
java -Dfile.encoding=UTF-8 ide.main.Start -cp .
```

### Como gerar um arquivo JAR

Para gerar um arquivo JAR, você precisa ter o programa compilado (isto é, os arquivos .class), então execute:

Você pode usar `make jar` para gerar (requer make instalado). Ele vai executar o seguinte comando:

_O comando `make createjar` também funciona, mas você precisa executar `make manifest` primeiro e `make clean` depois ou deletar o arquivo `manifest.txt`._

```batch
cd bin
jar cfm Boot-IDE.jar ../manifest.txt *
```

### Como gerar um arquivo de manifesto

Para gerar um arquivo JAR, você precisa ter o programa compilado (isto é, os arquivos .class). Aqui está como criar um:

Você pode usar `make manifest` também (requer make instalado). Ele vai executar os seguintes passos:

- Crie um arquivo .txt na pasta base;
- Escreva essa linha nele:

```
Main-Class: ide.main.Start
```

### Como executar um arquivo JAR

Gerado o arquivo JAR _(veja Como gerar um arquivo JAR)_, você pode executá-lo pelo comando:

Você pode usar `make launchjar` para executar (requer make instalado). Ele vai executar o seguinte comando:

```batch
java -jar Boot-IDE.jar
```

### Como gerar um arquivo executável do Windows

Gerado o arquivo JAR _(veja Como gerar um arquivo JAR)_, você pode gerá-lo pelo comando:

Você pode usar `make exe` para executar (requer make e Launch4J instalados). Ele vai executar o seguinte comando:

```batch
launch4jc exe-template.xml
```

### Como Usar

Na primeira vez que você abre a Boot IDE, não tem nenhuma pasta carregada, então clique no botão "Selecionar Pasta Base" e selecione uma pasta. <br />
Depois, clique no arquivo que deseja editar e comece a programar!
