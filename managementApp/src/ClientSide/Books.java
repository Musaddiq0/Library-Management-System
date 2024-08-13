package ClientSide;

public class Books {
    private String Author;
    private String Title;
    private String Genre;
    private int quantity;
    private int BookID;

    public Books (String Author,String Title,String Genre,int quantity,int BookID){
        this.Author = Author;
        this.BookID =  BookID;
        this.Genre =Genre;
        this.quantity =quantity;
        this.Title = Title;

    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getBookID() {
        return BookID;
    }

    public void setBookID(int bookID) {
        BookID = bookID;
    }
}
