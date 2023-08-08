package de.mediathekview.fimlistmerger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilmlistSplitterBean implements Processor {
  Logger LOG = LoggerFactory.getLogger(FilmlistSplitterBean.class);

  @Value("${FilmlistSplitterBean.messageChunkSize}")
  protected int PARTITION_SIZE;
    
  @Override
  public void process(Exchange exchange) throws Exception {
      Filmlist filmlist = exchange.getIn().getBody(Filmlist.class);
      List<List<Film>> result = removeDuplicatesAndMerge(removeDuplicates(filmlist));
      exchange.getIn().setBody(result);
      
  }
  
  public List<List<Film>> removeDuplicates(Filmlist filmlist) throws Exception {
    HashMap<Integer, List<Film>> makeUnique = new HashMap<>();
    filmlist.getFilms().values().forEach( film -> {
        //LOG.info("Object " + film );
        List<Film> x = new ArrayList<>();
        x.add(film);
        Object duplicate = makeUnique.put(
                Objects.hash(film.getSender(), film.getTitel(), film.getThema(), film.getDuration())
                ,x);
        if (duplicate != null)
            LOG.info("Removed duplicate element " + duplicate );
    });
    LOG.info("removeDuplicates final size " + makeUnique.size() );
    return new ArrayList<List<Film>>(makeUnique.values());
  }
  
  public List<List<Film>> removeDuplicatesAndMerge(List<List<Film>> filmlist) throws Exception {
      return  partition(filmlist, filmlist.size()/PARTITION_SIZE);
    }
  
  public <T> List<List<T>> partition(List<List<T>> inputList, int partitions){
      if (PARTITION_SIZE <= 0 || partitions <= 0) {
          return inputList;
      }
      
      List<List<T>> result = new ArrayList<>(partitions);
      for(int i = 0; i < partitions; i++)
          result.add(new ArrayList<>());

      for(int i = 0; i < inputList.size(); i++)
          result.get(i % partitions).add(inputList.get(i).get(0));

      LOG.info("paritioned into " + result.size() + " groups" );
      
      return result;
  }

}
