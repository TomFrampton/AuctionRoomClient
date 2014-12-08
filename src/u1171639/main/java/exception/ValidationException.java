package u1171639.main.java.exception;

import java.util.List;

import net.sf.oval.ConstraintViolation;

public class ValidationException extends AuctionException {
	private List<ConstraintViolation> violations;
	
	public ValidationException(String error, List<ConstraintViolation> violations) {
		super(error);
		this.violations = violations;
	}

	public List<ConstraintViolation> getViolations() {
		return violations;
	}

	public void setViolations(List<ConstraintViolation> violations) {
		this.violations = violations;
	}
}
