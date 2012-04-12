package bigs.core.exceptions;

public class BIGSTableExistsException extends Exception {

	private static final long serialVersionUID = 1L;

	public BIGSTableExistsException (String msg) {
        super(msg);
    }
}
