const csrfTokenInput = document.querySelector('input[name="_csrf"]');
const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';
const inviteEl = document.querySelector("#inviteDiv");
const userId = document.body.dataset.userId;
let acceptedInvitation = false;
let inviterUserId = null;
let createdGame = false;

if( inviteEl && inviteEl.getAttribute("name")) {
    console.log("You have invited Someone!");
    prompt(`Waining for ${inviteEl.getAttribute("name")} to respond...`);
};

function fetchData() {
    if (!userId) {
        return;
    }
    if(!acceptedInvitation) {
        console.log("fetching data ...");
        fetch(`/invited/${userId}/getInvites`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
            if(!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
            .then(data => {
            data.forEach(invite => {
                acceptedInvitation = confirm(`You have been invited to a game. Accept?`);
                inviterUserId = invite.inviterId;
            }
            )
        })
            .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        })
    }
    if(acceptedInvitation && !createdGame){
        console.log(`inviterId is ${inviterUserId} and the type is ${typeof inviterUserId}`);
        console.log(`inviteeId is ${userId} and the type is ${typeof userId}`);
        fetch(`/createAGame`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ inviterId: inviterUserId, inviteeId: userId })
        })
            .then(r => {
            if (!r.ok) throw new Error(`HTTP ${r.status}`);
            return r.json();
        })
            .then(data => {
            if (data.url) window.location = data.url;
        });

        createdGame = true;
    }
};




const intervalId = setInterval(fetchData, 500);


