package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	Map<Integer,Player> idMap;
	PremierLeagueDAO dao;
	Graph<Player,DefaultWeightedEdge> grafo;
	
	// Top Player
	Player topPlayer = null;
	List<Player> battuti;
	
	// Dream Team
	List<Player> dreamTeam ;
	int gradoTitolaritaMigliore;
	
	public Model() {
		this.dao = new PremierLeagueDAO();
	}
	
	public void creaGrafo(double mediaGoal) {
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		this.idMap = new HashMap<>();
		this.dao.getVertici(mediaGoal, idMap);
		
		// Aggiungo i vertici
		Graphs.addAllVertices(this.grafo, idMap.values());
		
		// Aggiungo gli archi
		for(Adiacenza a : this.dao.getAllAdiacenze(idMap)) {
			if(this.grafo.containsVertex(a.getP1()) && this.grafo.containsVertex(a.getP2())) {
				if(a.getPeso()>0) {
					Graphs.addEdgeWithVertices(this.grafo, a.getP1(), a.getP2(), a.getPeso());
				}
				else {
					Graphs.addEdgeWithVertices(this.grafo, a.getP2(), a.getP1(), ((double)-1)*a.getPeso());
				}
			}
		}
		
		// Top player
		int max = 0;
		for(Player p : this.grafo.vertexSet()) {
			int giocatoriBattuti = this.grafo.outDegreeOf(p);
			if(giocatoriBattuti>max) {
				max = giocatoriBattuti;
				topPlayer = p;
				battuti = new LinkedList<>();
				for(DefaultWeightedEdge arco : this.grafo.outgoingEdgesOf(p))
					battuti.add(this.grafo.getEdgeTarget(arco));
			}
		}
	}
	
	public String getNumeroVertici() {
		return "Numero di vertici: " +this.grafo.vertexSet().size();
	}
	
	public String getNumeroArchi() {
		return "\nNumero archi: " +this.grafo.edgeSet().size();
	}
	
	public String getTopPlayer() {
		String risultato = "\nTOP PLAYER: " +this.topPlayer.name +"\n\n" +"AVVERSARI BATTUTI: \n";
		
		for(Player p : this.battuti) {
			risultato += p.name +"\n";
		}
		
		return risultato;
		
	}
	
	
	
	// Dream Team
	public String cercaDreamTeam(int k) {
		List<Player> parziale = new LinkedList<>();
		this.dreamTeam = new LinkedList<>();
		gradoTitolaritaMigliore = 0;
		
		ricorsivo(parziale,new ArrayList<Player>(this.grafo.vertexSet()),k);
		
		String risultato = "\n\nDREAM TEAM - grado di titolarità: " +gradoTitolaritaMigliore + "\n\n";
		
		for(Player p : this.dreamTeam)
			risultato += p.name +"\n";
		
		return risultato;
	}
	
	public void ricorsivo(List<Player> parziale, List<Player> rimanenti, int k) {
		// Caso terminale
		if(parziale.size()==k) {
			int grado = gradoTitolarita(parziale);
			if(grado>gradoTitolaritaMigliore) {
				dreamTeam = new LinkedList<>(parziale);
				gradoTitolaritaMigliore = grado;
			}
			return;
		}
		
		// ... altrimenti
		for(Player prossimo : rimanenti) {
			// Controllo di non aver già inserito prossimo in parziale
			if(!parziale.contains(prossimo)) {
				// Provo ad aggiungere questo giocatore e tolgo i successori dalla lista dei rimanenti
				parziale.add(prossimo);
				List<Player> nuoviRimanenti = new LinkedList<>(rimanenti);
				nuoviRimanenti.removeAll(Graphs.successorListOf(this.grafo, prossimo));
				ricorsivo(parziale,nuoviRimanenti,k);
				parziale.remove(prossimo);
			}
			
		}
	}

	/**
	 * Restituisce il grado di titolarità complessivo della lista di giocatori passata come parametro
	 * @param parziale
	 * @return
	 */
	private int gradoTitolarita(List<Player> team) {
		int entranti;
		int uscenti;
		int gradoTitolarita = 0;
		
		for(Player p : team) {
			entranti = 0;
			uscenti = 0;
			
			for(DefaultWeightedEdge arco : this.grafo.outgoingEdgesOf(p)) {
				uscenti += (int)this.grafo.getEdgeWeight(arco);
			}
			
			for(DefaultWeightedEdge arco : this.grafo.incomingEdgesOf(p)) {
				entranti += (int)this.grafo.getEdgeWeight(arco);
			}
			
			gradoTitolarita += (uscenti-entranti);
			
		}
		
		return gradoTitolarita;
	}

}
