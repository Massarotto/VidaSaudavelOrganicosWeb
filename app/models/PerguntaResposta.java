/**
 * 
 */
package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import play.db.jpa.Model;

/**
 * @author hpadmin
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="PERGUNTA_RESPOSTA")
public class PerguntaResposta extends Model implements Serializable {
	
	public PerguntaResposta() {
	}
	
	public PerguntaResposta(Pergunta pergunta, Resposta resposta) {
		this.pergunta = pergunta;
		this.resposta = resposta;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Pergunta pergunta;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private Resposta resposta;

	/**
	 * @return the pergunta
	 */
	public Pergunta getPergunta() {
		return pergunta;
	}

	/**
	 * @param pergunta the pergunta to set
	 */
	public void setPergunta(Pergunta pergunta) {
		this.pergunta = pergunta;
	}

	/**
	 * @return the resposta
	 */
	public Resposta getResposta() {
		return resposta;
	}

	/**
	 * @param resposta the resposta to set
	 */
	public void setResposta(Resposta resposta) {
		this.resposta = resposta;
	}
	
}
