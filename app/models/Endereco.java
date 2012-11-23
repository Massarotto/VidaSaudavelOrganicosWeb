package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.validation.Min;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name="ENDERECO")
public class Endereco extends Model implements Serializable {
	
	public enum UF {
		ES("ES", "Espírito Santo"),
		SP("SP", "São Paulo"),
		MG("MG", "Minas Gerais"),
		RJ("RJ", "Rio de Janeiro"),
		AP("AP", "Amapá"),
		AC("AC", "Acre"),
		AM("AM", "Amazonas"),
		BA("BA", "Bahia"),
		CE("CE", "Ceará"),
		RN("RN", "Rio Grande do Norte"),
		RR("RR", "Roraima"),
		RO("RO", "Rondônia"),
		DF("DF", "Distrito Federal"),
		GO("GO", "Goiás"),
		PE("PE", "Pernambuco"),
		PB("PB", "Paraíba"),
		PI("PI", "Piauí"),
		RS("RS", "Rio Grande do Sul"),
		MA("MA", "Maranhão"),
		PA("PA", "Pará"),
		AL("AL", "Alagoas"),
		MS("MS", "Mato Grosso do Sul"),
		MT("MT", "Mato Grosso"),
		PR("PR", "Paraná"),
		SC("SC", "Santa Catarina"),
		SE("SE", "Sergipe"),
		TO("TO", "Tocantins");
		
		private String siglaUF;
		private String descricaoUF;
		
		private UF(String siglaUF, String descricaoUF) {
			this.siglaUF = siglaUF;
			this.descricaoUF = descricaoUF;
		}
		
		/**
		 * @return the siglaUF
		 */
		public String getSiglaUF() {
			return siglaUF;
		}

		/**
		 * @param siglaUF the siglaUF to set
		 */
		public void setSiglaUF(String siglaUF) {
			this.siglaUF = siglaUF;
		}

		/**
		 * @return the capital
		 */
		public String getDescricaoUF() {
			return descricaoUF;
		}

		/**
		 * @param capital the capital to set
		 */
		public void setDescricaoUF(String descricaoUF) {
			this.descricaoUF = descricaoUF;
		}
		
		/**
		 * Pesquisa a UF de acordo com o nome do estado
		 * @param descrUF
		 * @return
		 */
		public static String findUF(String descrUF) {
			for(UF _uf : UF.values()) {
				if(_uf.getDescricaoUF().equalsIgnoreCase(descrUF))
					return _uf.getSiglaUF(); 
			}
			return null;
		}
	}
	
	@Required(message="message.required.endereco.logradouro")
	@Column(name="LOGRADOURO", length=300, nullable=false)
	private String logradouro;
	
	@Required(message="message.required.endereco.numero")
	@Min(value=1, message="message.validation.endereco.numero")
	@Column(name="NUMERO", nullable=false, length=6)
	private Integer numero;
	
	@Column(name="COMPLEMENTO", nullable=true, length=100)
	private String complemento;
	
	@Required(message="message.required.endereco.bairro")
	@Column(name="BAIRRO", length=150, nullable=false)
	private String bairro;
	
	@Required(message="message.required.endereco.cidade")
	@Column(name="CIDADE", length=150, nullable=false)
	private String cidade;
	
	@Required(message="message.required.endereco.uf")
	@Column(name="UF", length=2, nullable=false)
	private String uf;
	
	@Required(message="message.required.endereco.cep")
	@MinSize(value=8, message="message.minsize.endereco.cep")
	@Column(name="CEP", length=9, nullable=false)
	private String cep;
	
	@Required(message="message.required.endereco.tipo")
	@Column(name="TIPO_ENDERECO", length=15, nullable=false)
	private String tipoEndereco;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Cliente cliente = null;

	/**
	 * @return the logradouro
	 */
	public String getLogradouro() {
		return logradouro;
	}

	/**
	 * @param logradouro the logradouro to set
	 */
	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	/**
	 * @return the numero
	 */
	public Integer getNumero() {
		return numero;
	}

	/**
	 * @param numero the numero to set
	 */
	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	/**
	 * @return the complemento
	 */
	public String getComplemento() {
		return complemento;
	}

	/**
	 * @param complemento the complemento to set
	 */
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}
	
	/**
	 * @return the bairro
	 */
	public String getBairro() {
		return bairro;
	}

	/**
	 * @param bairro the bairro to set
	 */
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	/**
	 * @return the cidade
	 */
	public String getCidade() {
		return cidade;
	}

	/**
	 * @param cidade the cidade to set
	 */
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	/**
	 * @return the uf
	 */
	public String getUf() {
		return uf;
	}

	/**
	 * @param uf the uf to set
	 */
	public void setUf(String uf) {
		this.uf = uf;
	}

	/**
	 * @return the cep
	 */
	public String getCep() {
		return cep;
	}

	/**
	 * @param cep the cep to set
	 */
	public void setCep(String cep) {
		this.cep = cep;
	}

	/**
	 * @return the tipoEndereco
	 */
	public String getTipoEndereco() {
		return tipoEndereco;
	}

	/**
	 * @param tipoEndereco the tipoEndereco to set
	 */
	public void setTipoEndereco(String tipoEndereco) {
		this.tipoEndereco = tipoEndereco;
	}

	/**
	 * @return the cliente
	 */
	public Cliente getCliente() {
		return cliente;
	}

	/**
	 * @param cliente the cliente to set
	 */
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	@Override
	public String toString() {
		StringBuilder campos = new StringBuilder();
		campos.append(this.logradouro).append("\n");
		campos.append(this.numero).append("\n");
		campos.append(this.complemento).append("\n");
		campos.append(this.bairro).append("\n");
		campos.append(this.cidade).append("\n");
		campos.append(this.uf).append("\n");
		campos.append(this.cep).append("\n");
		campos.append(this.tipoEndereco).append("\n");
		
		return campos.toString();
	}

}
