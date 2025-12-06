const { userEmail: currentUserEmail, userId, csrfToken } = document.body.dataset;
const currentUserId = userId ? Number(userId) : null;
const socket = new SockJS('/ws');
const stomp = Stomp.over(socket);
stomp.connect({}, () => {
    if (!currentUserEmail) {
        console.warn('No email available for invite subscriptions.');
        return;
    }
    stomp.subscribe(`/topic/invites/${currentUserEmail}`, frame => {
        const invite = JSON.parse(frame.body);
        if (invite.hasOwnProperty('accepted')) {
            const sameUser = invite.toUserId === currentUserId || invite.fromUserId === currentUserId;
            const responder = invite.fromName || invite.fromUserId;
            const message = invite.accepted
                ? `Your invite was accepted by ${responder}`
                : `Your invite was declined by ${responder}.`;

            if (invite.accepted && invite.gameId && sameUser) {
                window.location = `/game/${invite.gameId}`;
                return;
            }
            if (sameUser) {
                showResponseModal(message);
            }
        } else {
            // This is an invite
            showInviteModal(invite);
        }
    });
});

function showWaiting() {
    alert('Invite sent. Waiting for response...')
}

function showResponseModal(message) {
    const backdrop = document.getElementById('responseModal');
    const messageEl = document.getElementById('responseMessage');
    const closeBtn = document.getElementById('responseClose');
    messageEl.textContent = message;
    const cleanup = () => {
        backdrop.classList.remove('is-visible');
        closeBtn.onclick = null;
    };
    closeBtn.onclick = cleanup;
    backdrop.classList.add('is-visible');
}

function showInviteModal(invite) {
    const backdrop = document.getElementById('inviteModal');
    const message = document.getElementById('inviteMessage');
    const acceptBtn = document.getElementById('inviteAccept');
    const declineBtn = document.getElementById('inviteDecline');
    message.textContent = `Game invite from ${invite.fromName}. Accept?`;
    const cleanup = () => {
        backdrop.classList.remove('is-visible');
        acceptBtn.onclick = null;
        declineBtn.onclick = null;
    };
    acceptBtn.onclick = () => {
        respondToInvite(invite.fromUserId, true);
        cleanup();
    };
    declineBtn.onclick = () => {
        respondToInvite(invite.fromUserId, false);
        cleanup();
    };
    backdrop.classList.add('is-visible');
}

function respondToInvite(inviterId, accepted) {
    fetch('/invite/respond', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken || ''
        },
        body: JSON.stringify({
            inviterId: inviterId,
            accepted: accepted
        })
    }).then(resp => resp.ok ? resp.json() : null)
      .then(data => {
          if (!data) return;
          if (accepted && data.gameId) {
              window.location = `/game/${data.gameId}`;
          }
      })
      .catch(err => console.error('Invite response failed', err));
}
//        - User clicks “Invite to Game” → POST /invite with inviteeId.
//        - Controller looks up the inviter, calls inviteService.sendInvite(...).
//        - Service publishes to /user/{inviteeId}/queue/invites.
//        - The broker delivers to clients subscribed to /user/queue/invites; the invitee sees the
//        alert.
//
//        Adjust field names/types to match your User entity and existing extractName/email helpers.
//
