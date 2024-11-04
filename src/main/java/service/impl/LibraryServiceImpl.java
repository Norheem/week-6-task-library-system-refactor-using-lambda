package service.impl;

import baseClass.Person;
import model.Book;
import model.Librarian;
import model.Library;
import service.LibraryService;

import java.util.*;

public class LibraryServiceImpl implements LibraryService {
    Library library = new Library();

    private final HashMap<String, List<Book>> booksList = library.getBookOnShelf();

    private final Set<Integer> isExist = library.getIsIdBookExist();

    @Override
    public void addToQueue(Person person) {
        try {
            Optional.ofNullable(person)
                    .ifPresentOrElse(
                            p -> Optional.ofNullable(library)
                                    .map(lib -> lib.getPersonOnQueue())
                                    .ifPresentOrElse(
                                            queue -> queue.add(p),
                                            () -> System.out.println("Queue is not available in the library.")
                                    ),
                            () -> System.out.println("Cannot add to queue: Person is null.")
                    );
        } catch (Exception e) {
            System.err.println("An error occurred while adding a person to the queue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void implementFIFO(Person person) {

        try {
            Queue<Person> fifoQueue = library.getListUsingFifo();
            if (person != null && fifoQueue != null) {
                fifoQueue.add(person);
                System.out.println("Person added to FIFO queue.");

                // Give book to the next person in FIFO order
                Person nextPerson = fifoQueue.poll();
                if (nextPerson != null) {
                    Optional<Book> availableBook = booksList.values().stream()
                            .flatMap(list -> list.stream())
                            .filter(book -> book != null && book.getCopies() > 0)
                            .findFirst();

                    availableBook.ifPresentOrElse(book -> {
                        new BookServiceImpl().removeCopy(book);
                        System.out.printf("Book ID: %d titled '%s' has been given to %s %s (%s).%n",
                                book.getId(), book.getBookTitle(), nextPerson.getFirstName(),
                                nextPerson.getLastName(), nextPerson.getRole());
                    }, () -> System.out.println("No available book to give."));
                } else {
                    System.out.println("No one in queue to receive a book.");
                }
            } else {
                System.out.println("Cannot add to FIFO: Person or queue is null.");
            }
        } catch (Exception e) {
            System.err.println("Error in FIFO implementation: " + e.getMessage());
            e.printStackTrace();
        }
    }



    @Override
    public void addABook(Librarian librarian, Book book) {
        try {
            Optional.ofNullable(book)
                    .map(b -> b.getId())
                    .filter(id -> !isExist.contains(id))
                    .ifPresentOrElse(id -> {
                        isExist.add(id);

                        booksList.computeIfAbsent(book.getBookTitle(), title -> new ArrayList<>())
                                .add(book);

                    }, () -> System.out.println("Book ID " + book.getId() + " is already used by another book in the library."));

        } catch (Exception e) {
            System.err.println("An error occurred while adding the book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String giveBook(Librarian librarian, int bookId, String bookTitle) {
        try {
            Queue<Person> theQueue = Optional.ofNullable(library)
                    .map(Library::getPersonOnQueue)
                    .filter(queue -> !queue.isEmpty())
                    .orElseThrow(() -> new IllegalStateException("No one is at the library to borrow the book."));

            List<Book> listOfBooksInLibrary = Optional.ofNullable(library)
                    .map(lib -> lib.getBookOnShelf().get(bookTitle))
                    .orElse(List.of());

            Book availableBook = listOfBooksInLibrary.stream()
                    .filter(book -> book.getId() == bookId && book.getCopies() > 0)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Book with ID " + bookId + " and title " + bookTitle + " is either not available or all copies are borrowed."
                    ));


            Person nextPersonOnQueue = theQueue.poll();
            new BookServiceImpl().removeCopy(availableBook);

            return String.format("Book ID: %d with title '%s' has been given to %s %s, the %s, by %s %s, the %s.",
                    bookId,
                    availableBook.getBookTitle(),
                    nextPersonOnQueue.getFirstName(),
                    nextPersonOnQueue.getLastName(),
                    nextPersonOnQueue.getRole(),
                    librarian.getFirstName(),
                    librarian.getLastName(),
                    librarian.getRole()
            );

        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An unexpected error occurred while attempting to give the book.";
        }
    }
}
