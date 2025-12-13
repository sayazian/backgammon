let stompClient = null;
let currentUserId = null;

function connectStomp() {
    currentUserId = document.body.getAttribute('data-user-id');
    if (!currentUserId) {
        console.error('Current user id not found on page.');
        return;
    }

    const socket = new SockJS('/ws');
    const StompLib = window.Stomp || (window.StompJs && window.StompJs.Stomp);
    if (!StompLib) {
        console.error('STOMP library not loaded');
        return;
    }
    stompClient = StompLib.over(socket);

    stompClient.connect({}, frame => {
        console.log('Connected: ' + frame);

        // Subscribe to per-user topics keyed by userId
        stompClient.subscribe(`/topic/invitations/${currentUserId}`, message => {
            const invite = JSON.parse(message.body);
            onInviteReceived(invite);
        });

        stompClient.subscribe(`/topic/invitations/responses/${currentUserId}`, message => {
            const response = JSON.parse(message.body);
            onInviteResponse(response);
        });
    }, error => {
        console.error('STOMP error', error);
    });
}

function sendInvite(toUserId, toUserName) {
    if (!stompClient || !stompClient.connected) {
        console.warn('STOMP not connected yet.');
        return;
    }

    const message = prompt(`Message for user ${toUserName}`, 'Want to play a game?') || '';
    const payload = {
        toUserId: Number(toUserId),
        message: message
    };
    stompClient.send('/app/invite', {}, JSON.stringify(payload));
}

function onInviteReceived(invite) {
    const text = `User ${invite.fromUserName} invited you to a game.\n\nMessage: ${invite.message || ''}`;
    const accepted = confirm(text + '\n\nClick OK to accept, Cancel to decline.');

    const response = {
        toUserId: invite.fromUserId,
        gameId: invite.gameId || null,
        accepted: accepted
    };

    stompClient.send('/app/invite/response', {}, JSON.stringify(response));

    if (accepted && response.gameId) {
        window.location.href = `/games/${response.gameId}`;
    }
}

function onInviteResponse(response) {
    if (response.accepted) {
        alert(`User ${response.fromUserName || response.fromUserId} accepted your invite!`);
        // Only redirect if your server really has a game page
        if (response.gameId) {
            window.location.href = `/games/${response.gameId}`;
            console.log('Game accepted, gameId:', response.gameId);
        }
    } else {
        alert(`User ${response.fromUserName || response.fromUserId} declined your invite.`);
    }
}

function wireInviteForms() {
    document.querySelectorAll('.invite-form').forEach(form => {
        form.addEventListener('submit', event => {
            event.preventDefault();
            const toUserId = form.dataset.inviteeId;
            const toUserName = form.dataset.inviteeName;
            sendInvite(toUserId, toUserName);
        });
    });
}

window.addEventListener('DOMContentLoaded', () => {
    connectStomp();
    wireInviteForms();
});
