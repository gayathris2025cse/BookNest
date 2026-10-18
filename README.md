# BookNest Library Management System

A simple Java library project with Swing and console versions. Both use JDBC and MySQL.

## Required software

- JDK 8 or newer
- MySQL Server and the `mysql` command-line client
- MySQL Connector/J JAR file

## Set up the database

Start MySQL. In a terminal, run this command from the BookNest folder:

```sh
mysql -u root -p < booknest.sql
```

This creates `booknest_db`, its three tables, and sample books and members. Enter your local MySQL password when asked. Running the file again will not add duplicate samples.

## Set your database password

The app uses the MySQL `root` account. Set its password in the Terminal session before running the app. On macOS or Linux, run `read -s BOOKNEST_DB_PASSWORD`, type the password (it will not display), press Enter, then run `export BOOKNEST_DB_PASSWORD`. This keeps the password out of the Java files. On Windows Command Prompt, use `set /p BOOKNEST_DB_PASSWORD=MySQL password: `.

## Add the JDBC driver and run

Download MySQL Connector/J from MySQL and put its JAR in a `lib` folder inside BookNest. The `lib/*` part of the commands includes that JAR automatically.

On macOS or Linux:

```sh
mkdir -p out
javac -cp 'lib/*' -d out src/*.java
java -cp 'out:lib/*' LibraryApp
```

On Windows Command Prompt:

```bat
mkdir out
javac -cp "lib/*" -d out src\*.java
java -cp "out;lib/*" LibraryApp
```

Keep MySQL running while using the app. The buttons on the left open small input forms or show a table in the main window.

## Run the console version

Use the same database, JDBC driver, and `BOOKNEST_DB_PASSWORD` setting described above. From the BookNest folder, compile and run the text menu instead of `LibraryApp`:

On macOS or Linux:

```sh
javac -cp 'lib/*' -d out src/*.java
java -cp 'out:lib/*' ConsoleLibraryApp
```

On Windows Command Prompt:

```bat
javac -cp "lib/*" -d out src\*.java
java -cp "out;lib/*" ConsoleLibraryApp
```

Enter a number from 0 to 9. Enter 0 to exit. The console and Swing versions share the same MySQL records.

## Quick feature check

1. Show all books and members. Sample records should appear.
2. Add a book and member, then try their IDs again to see duplicate warnings.
3. Issue a book and check that its available copies decrease. Issue three books to one member, then check that a fourth is rejected.
4. Return a book after 12 days. The fine should be Rs. 10 and its available copies should increase.
5. Try returning it again; the app should reject the return.
6. Search for a book by ID and by part of its title. Open Library Summary.
7. Close and reopen the app. The records should still be present.
