package service.impl;

import model.Book;
import service.BookService;

import java.util.Optional;

public class BookServiceImpl implements BookService {


    @Override
    public void removeCopy(Book book) {
        try {
            Optional.ofNullable(book)
                    .filter(b -> b.getCopies() > 0)
                    .ifPresentOrElse(
                            b -> b.setCopies(b.getCopies() - 1),
                            () -> System.out.println("No copies available to remove.")
                    );
        } catch (Exception e) {
            System.err.println("An error occurred while removing a copy: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void addCopy(Book book) {
        try {
            Optional.ofNullable(book)
                    .ifPresentOrElse(
                            b -> b.setCopies(b.getCopies() + 1),
                            () -> System.out.println("Cannot add a copy: Book is null.")
                    );
        } catch (Exception e) {
            System.err.println("An error occurred while adding a copy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
