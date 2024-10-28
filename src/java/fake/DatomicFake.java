package fake;

import datomic.Database;
import datomic.Entity;
import datomic.Datom;
import datomic.Attribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Stream;

public class DatomicFake implements Database {


    private final Set<Object[]> dataSet = new HashSet<>();


    public DatomicFake (Set<Object[]> dados) {
        this.dataSet.addAll(dados);
    }

    /**
     * Implementação do método `pull` para simular uma busca em um "banco de dados"
     * 
     * @param dbOrId    - parâmetro ignorado na simulação
     * @param selector  - conjunto de atributos a serem selecionados
     * @param entityId  - identificador da entidade que deseja-se buscar
     * @return Map<Object, Object> com os pares chave-valor correspondentes à entidade
     */
    @Override
    public Map<Object, Object> pull(Object dbOrId, Object selector) {
        System.out.println("opaaa...");
        throw new UnsupportedOperationException("Método não suportado.");
    }    

    /**
     * Implementação do método `pull` para simular uma busca em um "banco de dados"
     * 
     * @param dbOrId    - parâmetro ignorado na simulação
     * @param selector  - conjunto de atributos a serem selecionados
     * @param entityId  - identificador da entidade que deseja-se buscar
     * @return Map<Object, Object> com os pares chave-valor correspondentes à entidade
     */
    @Override
    public Map<Object, Object> pull(Object dbOrId, Object selector, Object entityId) {
        System.out.println("opaaa pull 2...");
        Map<Object, Object> entityMap = new HashMap<>();
        
        // Filtra as entradas que correspondem ao entityId
        for (Object[] entry : dataSet) {
            if (entry[0].equals(entityId)) {
                entityMap.put(entry[1], entry[2]);
            }
        }

        // Se o seletor for um Set, filtra apenas os atributos especificados
        if (selector instanceof Set) {
            Set<?> selectorSet = (Set<?>) selector;
            entityMap.keySet().retainAll(selectorSet);
        }
        
        return entityMap;
    }
    
    @Override
    public Database asOf(Object t) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Long asOfT() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Attribute attribute(Object attrId) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public long basisT() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Iterable<Datom> datoms(Object index, Object... components) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Map dbStats() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Object entid(Object entityId) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Object entidAt(Object partition, Object timePoint) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Entity entity(Object entityId) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Database filter(Predicate<Datom> pred) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Database filter(Object pred) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Database history() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public String id() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Object ident(Object idOrKey) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Stream<Object> indexPull(Object options) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Iterable<Datom> indexRange(Object attrid, Object start, Object end) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Object invoke(Object entityId, Object... args) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public boolean isFiltered() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public boolean isHistory() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public long nextT() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public List<Map> pullMany(Object pattern, List entityIds) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public List<Map> pullMany(Object pattern, List entityIds, Object otherObject) {
        throw new UnsupportedOperationException("Método não suportado.");
    }    

    @Override
    public Iterable<Datom> seekDatoms(Object index, Object... components) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Database since(Object t) {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Long sinceT() {
        throw new UnsupportedOperationException("Método não suportado.");
    }

    @Override
    public Map with(List txData) {
        throw new UnsupportedOperationException("Método não suportado.");
    }    
}