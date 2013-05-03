/**
 * 
 */
package models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import org.joda.time.Days;

import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * @author guerrafe
 *
 */
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Cacheable
@Entity
@Table(name="CESTA_ASSINATURA")
public class CestaAssinatura extends Model implements Serializable {

	private static final long serialVersionUID = 7438102087259438282L;
	
	public enum CestaAssinaturaPeriodo {
		SEMANAL(7, "Semanal"),
		QUINZENAL(15, "Quinzenal");
		
		private Integer periodicidade;
		private String descricao;
		
		private CestaAssinaturaPeriodo(Integer periodicidade, String descricao) {
			this.periodicidade = periodicidade;
			this.descricao = descricao;
		}
		
		/**
		 * @return the descricao
		 */
		public String getDescricao() {
			return descricao;
		}

		/**
		 * @param descricao the descricao to set
		 */
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}

		/**
		 * @return the periodo
		 */
		public Integer getPeriodicidade() {
			return periodicidade;
		}

		/**
		 * @param periodo the periodo to set
		 */
		public void setPeriodicidade(Integer periodicidade) {
			this.periodicidade = periodicidade;
		}
	}

	@ManyToOne(fetch=FetchType.LAZY)
	private Cliente cliente;
	
	@Temporal(TemporalType.DATE)
	@Column(name="DATA_INICIO", nullable=false)
	private Date dataInicio;
	
	@Temporal(TemporalType.DATE)
	@Column(name="DATA_ENVIO", nullable=false)
	private Date dataEnvio;
	
	@Column(name="FLAG_ATIVO", nullable=false)
	private Boolean ativo;
	
	@Required
	@Column(name="PERIODICIDADE", nullable=false)
	private CestaAssinaturaPeriodo periodo;
	
	@Required
	@Column(name="VALOR_CESTA", nullable=false, scale=2, precision=8)
	private BigDecimal valorCesta;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Pagamento pagamento;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="cestaAssinatura")
	private List<CestaAssinaturaProduto> listCestaAssinaturaProduto;
	
	public CestaAssinatura() {
		// TODO Auto-generated constructor stub
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

	/**
	 * @return the dataInicio
	 */
	public Date getDataInicio() {
		return this.dataInicio;
	}

	/**
	 * @param dataInicio the dataInicio to set
	 */
	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	/**
	 * @return the ativo
	 */
	public Boolean getAtivo() {
		return ativo;
	}

	/**
	 * @param ativo the ativo to set
	 */
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	/**
	 * @return the periodo
	 */
	public CestaAssinaturaPeriodo getPeriodo() {
		return periodo;
	}

	/**
	 * @param periodo the periodo to set
	 */
	public void setPeriodo(CestaAssinaturaPeriodo periodo) {
		this.periodo = periodo;
	}

	/**
	 * @return the listCestaAssinaturaProduto
	 */
	public List<CestaAssinaturaProduto> getListCestaAssinaturaProduto() {
		if(this.listCestaAssinaturaProduto==null)
			this.listCestaAssinaturaProduto = new ArrayList<CestaAssinaturaProduto>();
		
		return listCestaAssinaturaProduto;
	}

	/**
	 * @param listCestaAssinaturaProduto the listCestaAssinaturaProduto to set
	 */
	public void setListCestaAssinaturaProduto(List<CestaAssinaturaProduto> listCestaAssinaturaProduto) {
		this.listCestaAssinaturaProduto = listCestaAssinaturaProduto;
	}
	
	@Transient
	public void assignDataInicio() {
		Calendar dataInicial = new GregorianCalendar();
		int tuesday = Calendar.TUESDAY;
		int dayOfWeek = 0;
		
		if(dayOfWeek==tuesday) {
			setDataInicio(dataInicial.getTime());
			
		}else {
			while(dayOfWeek!=tuesday) {
				dayOfWeek = dataInicial.get(dataInicial.DAY_OF_WEEK);
				
				if(dayOfWeek==tuesday) {
					setDataInicio(dataInicial.getTime());
					
					break;
				}
				dataInicial.add(Calendar.DATE, 1);
			}
			Calendar dtIni = new GregorianCalendar();
			
			//Não pode extrapolar o período de entrega
			DateTime dataProxEntrega = new DateTime(dataInicial);
			DateTime sysDate = new DateTime(dtIni);
			
			if(Days.daysBetween(sysDate, dataProxEntrega).getDays()<2) {
				dtIni.add(Calendar.DATE, 7);
				setDataInicio(dtIni.getTime());
			}
		}
	}
	
	@Transient
	public void addDiasDataEnvio(Integer dias) {
		Calendar dataInicio = Calendar.getInstance();
		
		dataInicio.setTimeInMillis(getDataEnvio().getTime());
		dataInicio.add(Calendar.DAY_OF_MONTH, dias);
		
		setDataEnvio(dataInicio.getTime());
	}

	/**
	 * @return the valorCesta
	 */
	public BigDecimal getValorCesta() {
		return valorCesta;
	}

	/**
	 * @param valorCesta the valorCesta to set
	 */
	public void setValorCesta(BigDecimal valorCesta) {
		this.valorCesta = valorCesta;
	}

	/**
	 * @return the pagamento
	 */
	public Pagamento getPagamento() {
		return pagamento;
	}

	/**
	 * @param pagamento the pagamento to set
	 */
	public void setPagamento(Pagamento pagamento) {
		if(pagamento!=null)
			pagamento.setAssinatura(this);
		
		this.pagamento = pagamento;
	}

	/**
	 * @return the dataEnvio
	 */
	public Date getDataEnvio() {
		return dataEnvio;
	}

	/**
	 * @param dataEnvio the dataEnvio to set
	 */
	public void setDataEnvio(Date dataEnvio) {
		this.dataEnvio = dataEnvio;
	}

}
