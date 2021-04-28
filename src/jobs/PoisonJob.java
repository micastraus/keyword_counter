package jobs;

public class PoisonJob implements Job {
	
	public PoisonJob() {
		
	}

	@Override
	public JobType getType() {
		return JobType.POISON;
	}

}
