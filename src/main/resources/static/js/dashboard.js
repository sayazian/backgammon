let stompClient = null;
let currentUserId = null;
let inviteQueue = [];
let activeInvite = null;

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

        stompClient.subscribe(`/topic/invitations/status/${currentUserId}`, message => {
            const status = JSON.parse(message.body);
            onInviteStatus(status);
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
        toUserName: toUserName,
        message: message
    };
    stompClient.send('/app/invite', {}, JSON.stringify(payload));
}

function onInviteReceived(invite) {
    inviteQueue.push(invite);
    if (!activeInvite) {
        showNextInvite();
    }
}

function showNextInvite() {
    if (inviteQueue.length === 0) {
        activeInvite = null;
        hideInviteModal();
        return;
    }

    activeInvite = inviteQueue.shift();
    document.getElementById('invite-modal-title').textContent = `${activeInvite.fromUserName} invited you to a game`;
    document.getElementById('invite-modal-message').textContent = activeInvite.message || 'Want to play a game?';
    document.getElementById('invite-modal').classList.add('is-visible');
}

function hideInviteModal() {
    document.getElementById('invite-modal').classList.remove('is-visible');
}

function respondToActiveInvite(accepted) {
    if (!activeInvite) {
        return;
    }
    const response = {
        inviteId: activeInvite.inviteId,
        toUserId: activeInvite.fromUserId,
        accepted: accepted
    };

    stompClient.send('/app/invite/response', {}, JSON.stringify(response));

    if (accepted && response.gameId) {
        window.location.href = `/games/${response.gameId}`;
    }

    activeInvite = null;
    hideInviteModal();
    showNextInvite();
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

function onInviteStatus(status) {
    if (!status.delivered) {
        alert(`Could not deliver your invite to ${status.toUserName || status.toUserId}. ${status.reason || ''}`.trim());
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

function wireInviteModal() {
    const acceptButton = document.getElementById('invite-accept-button');
    const declineButton = document.getElementById('invite-decline-button');

    if (acceptButton) {
        acceptButton.addEventListener('click', () => respondToActiveInvite(true));
    }

    if (declineButton) {
        declineButton.addEventListener('click', () => respondToActiveInvite(false));
    }
}

window.addEventListener('DOMContentLoaded', () => {
    connectStomp();
    wireInviteForms();
    wireInviteModal();
});
