function sendNotification(doc:NotesDocument){
	emailBean.sendNotification(doc);
}

function saveDocument(doc:NotesDocument){
	if (!doc.isNewNote()){
		sendNotification(doc);
	}
	doc.computeWithForm(false, false);
	doc.save();
}

function changeStatus(doc:NotesDocument, newstatus:String){
	doc.replaceItemValue("Status", newstatus);
	if (newstatus == "Completed"){
		doc.replaceItemValue("CompletionDate", session.createDateTime(new Date()))
	}
	saveDocument(doc);
}