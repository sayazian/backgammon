let stompClient = null;
const form = document.getElementById('inviteForm');
const inviteeUserId = document.getElementById('inviteeId');

function connectStomp() {
    const socket = new SockJS('ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, frame =>{
        console.log('Connected: ' + frame);

        stompClient.subscribe('/user/queue/invitations', message => {
            const invite = JSON.parse(message.body);
            onInvitedReceived(invite);
        });

        stompClient.subscribe('/user/queue/invitations/responses', message => {
            const response = JSON.parse(message.body);
            onInviteResponse(response);
        });
    }, error => {
        console.error('STOMP error', error);
    });
}

function inviteUser(inviteeId) {
    const message = promopt(`Message for ${inviteeId}`, `Want to play a game?`);
    const payload = {
        toUsername: inviteeId,
        message: message
    };
    stompClient.send('/app/invite', {}, JSON.stringify(payload));
}

function onInviteReceived(invite) {
    const text = `${invite.fromUsername} invited you to a game.\n\nMessage: ${invite.message || ''}`;
    const accepted = confirm(text + "\n\nClick OK to accept, Cancel to decline.");

    const response = {
        toUsername: invite.fromUsername,
        gameId: invite.gameId || null,
        accepted: accepted
    };

    stompClient.send('/app/invite/response', {}, JSON.stringify(response));

    if(accepted) {
        // Option 1: server generates gameId and sends in a follow-up message
        // Option 2: redirect later when you receive a "game-started" event
    }
}

function onInviteResponse(response) {
    if(response.accepted) {
        alert(`${response.fromUsername} accepted your invite!`);

        //If response.gameId is set
        if(response.gameId) {
            window.location.href = `/game/${response.gameId}`;
        }
    } else {
        alert(`${response.fromUsername} declined your invite.`)
    }
}

window.addEventListener('DOMContentLoaded', connectStomp);
from.addEventListener('submit', inviteUser(inviteeUserId));
