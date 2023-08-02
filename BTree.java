import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * B+Tree Structure
 * Key - StudentId
 * Leaf Node should contain [key, recordId]
 */
class BTree {

    /**
     * Pointer to the root node.
     */
    private BTreeNode root;
    /**
     * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
     **/
    private int t;

    BTree(int t) {
        this.root = null;
        this.t = t;
    }

    long search(long studentId) {
        return searchHelper(root, studentId);
    }

    private long searchHelper(BTreeNode node, long studentId) {
        if (node == null) {
            System.out.println("Student with ID " + studentId + " not found.");
            return -1;
        }

        int i = 0;
        while (i < node.n && studentId > node.keys[i]) {
            i++;
        }

        if (i < node.n && studentId == node.keys[i]) {
            return node.values[i];
        } else if (node.leaf) {
            System.out.println("Student with ID " + studentId + " not found.");
            return -1;
        } else {
            return searchHelper(node.children[i], studentId);
        }
    }


    BTree insert(Student student) {
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = student.studentId;
            root.values[0] = student.recordId;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1) {
                BTreeNode newRoot = new BTreeNode(t, false);
                newRoot.children[0] = root;
                splitChild(newRoot, 0);
                int i = 0;
                if (newRoot.keys[0] < student.studentId) {
                    i++;
                }
                insertNonFull(newRoot.children[i], student);
                root = newRoot;
            } else {
                insertNonFull(root, student);
            }
        }
        writeToCSV(student); // Insert into student.csv
        return this;
    }

    private void writeToCSV(Student student) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/Student.csv", true))) {
            String studentInfo = student.studentId + "," +
                    student.studentName + "," +
                    student.major + "," +
                    student.level + "," +
                    student.age + "," +
                    student.recordId;
            bw.write(studentInfo);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to student.csv: " + e.getMessage());
        }
    }

    private void insertNonFull(BTreeNode node, Student student) {
        int i = node.n - 1;
        if (node.leaf) {
            while (i >= 0 && student.studentId < node.keys[i]) {
                node.keys[i + 1] = node.keys[i];
                node.values[i + 1] = node.values[i];
                i--;
            }
            node.keys[i + 1] = student.studentId;
            node.values[i + 1] = student.recordId;
            node.n++;
        } else {
            while (i >= 0 && student.studentId < node.keys[i]) {
                i--;
            }
            i++;
            if (node.children[i].n == 2 * t - 1) {
                splitChild(node, i);
                if (student.studentId > node.keys[i]) {
                    i++;
                }
            }
            insertNonFull(node.children[i], student);
        }
    }

    private void splitChild(BTreeNode parentNode, int childIndex) {
        BTreeNode child = parentNode.children[childIndex];
        BTreeNode newChild = new BTreeNode(t, child.leaf);
        parentNode.n++;

        // Copy keys and values from the child node to the new child node
        for (int j = 0; j < t - 1; j++) {
            newChild.keys[j] = child.keys[j + t];
            newChild.values[j] = child.values[j + t];
        }

        if (!child.leaf) {
            // Copy children pointers from the child node to the new child node
            for (int j = 0; j < t; j++) {
                newChild.children[j] = child.children[j + t];
            }
        }

        // Shift keys and children pointers in the parent node to accommodate the new child
        for (int j = parentNode.n - 1; j >= childIndex + 1; j--) {
            parentNode.children[j + 1] = parentNode.children[j];
        }

        for (int j = parentNode.n - 2; j >= childIndex; j--) {
            parentNode.keys[j + 1] = parentNode.keys[j];
            parentNode.values[j + 1] = parentNode.values[j];
        }

        // Update the parent node with the key and value from the middle of the child node
        parentNode.keys[childIndex] = child.keys[t - 1];
        parentNode.values[childIndex] = child.values[t - 1];

        // Update the sizes of the child and new child nodes
        child.n = t - 1;
        newChild.n = t - 1;

        // Link the new child node to the parent node
        parentNode.children[childIndex + 1] = newChild;
    }

    boolean delete(long studentId) {
        if (root == null) {
            return false; // The tree is empty, student not found
        }

        boolean isDeleted = deleteHelper(root, studentId);
        if (isDeleted) {
            deleteFromCSV(studentId); // Delete from student.csv if it exists
        }
        return isDeleted;
    }
    private void deleteFromCSV(long studentId) {
        File inputFile = new File("src/Student.csv");
        File tempFile = new File("src/temp.csv");

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                long currentStudentId = Long.parseLong(values[0]);
                if (currentStudentId != studentId) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading/writing from/to Student.csv: " + e.getMessage());
        }

        // Rename the temporary file to replace the original file
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Failed to update Student.csv.");
        }
    }


    private boolean deleteHelper(BTreeNode node, long studentId) {
        int index = findKeyIndex(node, studentId);

        if (index < node.n && node.keys[index] == studentId) {
            // The studentId is found in this node, handle the deletion
            if (node.leaf) {
                removeFromLeaf(node, index);
            } else {
                removeFromInternalNode(node, index);
            }
            return true;
        }

        if (node.leaf) {
            return false; // The studentId is not found in the tree
        }

        // Recurse into the appropriate child node
        boolean shouldDescend = (index == node.n || studentId < node.keys[index]);
        if (shouldDescend) {
            return deleteHelper(node.children[index], studentId);
        } else {
            return deleteHelper(node.children[index + 1], studentId);
        }
    }

    private int findKeyIndex(BTreeNode node, long studentId) {
        int index = 0;
        while (index < node.n && node.keys[index] < studentId) {
            index++;
        }
        return index;
    }

    private void removeFromLeaf(BTreeNode node, int index) {
        // Shift all keys and values one position to the left to remove the key-value pair
        for (int i = index; i < node.n - 1; i++) {
            node.keys[i] = node.keys[i + 1];
            node.values[i] = node.values[i + 1];
        }
        node.n--;
    }

    private void removeFromInternalNode(BTreeNode node, int index) {
        long studentId = node.keys[index];
        BTreeNode leftChild = node.children[index];
        BTreeNode rightChild = node.children[index + 1];

        if (leftChild.n >= t) {
            // Case 3a: If the left child has at least t keys, find the predecessor and recursively delete it
            long predecessorKey = getPredecessorKey(leftChild);
            node.keys[index] = predecessorKey;
            deleteHelper(leftChild, predecessorKey);
        } else if (rightChild.n >= t) {
            // Case 3a: If the right child has at least t keys, find the successor and recursively delete it
            long successorKey = getSuccessorKey(rightChild);
            node.keys[index] = successorKey;
            deleteHelper(rightChild, successorKey);
        } else {
            // Case 3b: If both left and right children have t-1 keys, merge the left and right children
            // and move the key from the current node to the merged node
            mergeChildren(node, index);
            deleteHelper(leftChild, studentId); // Recursively delete the key from the merged node
        }
    }

    private long getPredecessorKey(BTreeNode node) {
        // The predecessor key is the rightmost key in the rightmost leaf of the left child
        while (!node.leaf) {
            node = node.children[node.n];
        }
        return node.keys[node.n - 1];
    }

    private long getSuccessorKey(BTreeNode node) {
        // The successor key is the leftmost key in the leftmost leaf of the right child
        while (!node.leaf) {
            node = node.children[0];
        }
        return node.keys[0];
    }

    private void mergeChildren(BTreeNode node, int index) {
        BTreeNode leftChild = node.children[index];
        BTreeNode rightChild = node.children[index + 1];

        // Move the key from the current node to the left child
        leftChild.keys[leftChild.n] = node.keys[index];
        leftChild.values[leftChild.n] = node.values[index];
        leftChild.n++;

        // Move keys and values from the right child to the left child
        for (int i = 0; i < rightChild.n; i++) {
            leftChild.keys[leftChild.n + i] = rightChild.keys[i];
            leftChild.values[leftChild.n + i] = rightChild.values[i];
        }

        // If the nodes are not leaf nodes, also move children pointers
        if (!leftChild.leaf) {
            for (int i = 0; i <= rightChild.n; i++) {
                leftChild.children[leftChild.n + i] = rightChild.children[i];
            }
        }

        // Shift keys and children pointers in the current node to fill the gap
        for (int i = index + 1; i < node.n; i++) {
            node.keys[i - 1] = node.keys[i];
            node.values[i - 1] = node.values[i];
            node.children[i] = node.children[i + 1];
        }

        // Decrement the number of keys in the current node
        node.n--;

        // Release the right child node
        rightChild = null;
    }

    List<Long> print() {
        List<Long> listOfRecordID = new ArrayList<>();
        printHelper(root, listOfRecordID);
        return listOfRecordID;
    }

    private void printHelper(BTreeNode node, List<Long> listOfRecordID) {
        if (node != null) {
            if (node.leaf) {
                for (int i = 0; i < node.n; i++) {
                    listOfRecordID.add(node.values[i]);
                }
            } else {
                for (int i = 0; i < node.n + 1; i++) {
                    printHelper(node.children[i], listOfRecordID);
                }
            }
        }
    }
}
