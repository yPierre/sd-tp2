package pub;

import java.util.Set;
import java.util.SortedSet;

import core.Message;
import core.MessageImpl;
import core.PubSubCommand;

public class NotifyCommand implements PubSubCommand{

	@Override
	public Message execute(Message m, SortedSet<Message> log, Set<String> subscribers) {
		
		Message response = new MessageImpl();
		
		response.setContent("Message notified: " + m.getContent());
		
		response.setType("notify_ack");
		
		if(!log.contains(m)){
			log.add(m);			
		}
		
		System.out.println("Number of Log itens of an Observer " + m.getBrokerId() + " : " + log.size());
		
		return response;

	}

}

