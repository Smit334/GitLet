# GitLet

GitLet is a simplified version control system modeled after Git. It allows users to track changes in their project files, commit these changes, and navigate the history of their project.

## Features

- **Commit Tracking**: Track changes to your files and commit them to the repository.
- **Branching**: Create and manage branches to experiment with different versions of your project.
- **Merging**: Merge branches to integrate changes.
- **File Serialization**: Serialize and deserialize objects for debugging purposes.

## Usage

### Compilation

To compile the project, use the provided `Makefile`. The main targets are:

- `make`: Compiles the project.
- `make check`: Compiles and tests the project.
- `make clean`: Cleans up generated files.
- `make doc`: Generates Javadocs for the project.

### Running the Application

To run the application, execute the main class with:

```bash
java gitlet.Main
```

### Debugging

A debugging class DumpObj is provided to deserialize and dump objects to the console. Usage:
```bash
java gitlet.DumpObj FILE...
```
## Project Structure

- **`gitlet/Blob.java`**: Handles the serialization of file contents and their SHA-1 hash codes.
- **`gitlet/Commit.java`**: Manages commit objects, including their messages, timestamps, parent commits, and tracked files.
- **`gitlet/Diff.java`**: Provides methods to compare sequences of strings, compute longest common subsequences, and generate diffs.
- **`gitlet/DumpObj.java`**: A debugging class to deserialize and dump objects.

---

## Contribution

To contribute to this project:
1. Fork the repository.
2. Create a new branch.
3. Submit a pull request.

Ensure that your code:
- Follows the project's style guidelines.
- Passes all tests.

---

## License

This project is licensed under the terms of the MIT license.

---

## Contact

For any inquiries, please contact the repository owner [Smit334](https://github.com/Smit334).
