/**
 * 
 */
package util;

/**
 * @author guerrafe
 *
 */
public enum PeriodoEntrega {
	MANHA("Manhã"),
	TARDE("Tarde");

	private PeriodoEntrega(String periodo) {
		this.periodo = periodo;
	}
	
	private String periodo;

	/**
	 * @return the periodo
	 */
	public String getPeriodo() {
		return periodo;
	}

	/**
	 * @param periodo the periodo to set
	 */
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
}
