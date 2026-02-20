import React, { useMemo, useState } from 'react'
import { createRoot } from 'react-dom/client'
import axios from 'axios'
import './styles.css'

function basicAuthHeader(username, password) {
  return `Basic ${btoa(`${username}:${password}`)}`
}

function LoginPage({ onLogin, error }) {
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('admin')

  return (
    <div className="login-wrap">
      <h1>Solace DMQ Manager</h1>
      <div className="card">
        <label>Username</label>
        <input value={username} onChange={(e) => setUsername(e.target.value)} />
        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button onClick={() => onLogin({ username, password })}>Login</button>
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  )
}

function App() {
  const [auth, setAuth] = useState(null)
  const [error, setError] = useState('')
  const [dmqs, setDmqs] = useState([])
  const [selectedDmq, setSelectedDmq] = useState(null)
  const [messages, setMessages] = useState([])
  const [selectedRows, setSelectedRows] = useState([])
  const [detail, setDetail] = useState(null)

  const client = useMemo(() => {
    if (!auth) return null
    return axios.create({
      headers: { Authorization: basicAuthHeader(auth.username, auth.password) }
    })
  }, [auth])

  const loadDmqs = async (ax = client) => {
    const { data } = await ax.get('/api/dmqs')
    setDmqs(data)
  }

  const onLogin = async (credentials) => {
    try {
      const ax = axios.create({ headers: { Authorization: basicAuthHeader(credentials.username, credentials.password) } })
      await loadDmqs(ax)
      setAuth(credentials)
      setError('')
    } catch {
      setError('Login failed')
    }
  }

  const selectDmq = async (dmq) => {
    setSelectedDmq(dmq)
    const { data } = await client.get(`/api/dmqs/${encodeURIComponent(dmq.queueName)}/messages?limit=200`)
    setMessages(data)
    setSelectedRows([])
    setDetail(null)
  }

  const toggleSelected = (rowNumber) => {
    setSelectedRows((prev) => prev.includes(rowNumber) ? prev.filter((v) => v !== rowNumber) : [...prev, rowNumber])
  }

  const replayQueue = async (dmq) => {
    const messageCount = dmq.messageCount
    const confirmed = window.confirm(`Confirm moving ${messageCount} messages back to origin queue ${dmq.originQueueName}`)
    if (!confirmed) return
    const deleteFromDmqAfterCopy = window.confirm('Delete from DMQ After Copy? (OK = checked, Cancel = unchecked)')
    await client.post(`/api/dmqs/${encodeURIComponent(dmq.queueName)}/replay`, { deleteFromDmqAfterCopy })
    await loadDmqs()
    if (selectedDmq?.queueName === dmq.queueName) {
      await selectDmq(dmq)
    }
  }

  if (!auth) {
    return <LoginPage onLogin={onLogin} error={error} />
  }

  return (
    <div className="page">
      <h1>Solace DMQ Manager</h1>
      <div className="layout">
        <div className="card">
          <h2>Dead Message Queues</h2>
          <table>
            <thead><tr><th>Queue</th><th>Origin Queue</th><th>Messages</th><th>Action</th></tr></thead>
            <tbody>
              {dmqs.map((dmq) => (
                <tr key={dmq.queueName} className={selectedDmq?.queueName === dmq.queueName ? 'active' : ''}>
                  <td onClick={() => selectDmq(dmq)}>{dmq.queueName}</td>
                  <td>{dmq.originQueueName}</td>
                  <td>{dmq.messageCount}</td>
                  <td><button onClick={() => replayQueue(dmq)}>Replay to Origin Queue</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <h2>Messages {selectedDmq ? `in ${selectedDmq.queueName}` : ''}</h2>
          {selectedDmq && <p>Selected: {selectedRows.length}</p>}
          <div className="messages">
            {messages.map((m) => (
              <div key={m.rowNumber} className="msg-row" onClick={() => setDetail(m)}>
                <input
                  type="checkbox"
                  checked={selectedRows.includes(m.rowNumber)}
                  onChange={() => toggleSelected(m.rowNumber)}
                  onClick={(e) => e.stopPropagation()}
                />
                <span>{m.rowNumber}. {m.messageId || '(no message id)'}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <h2>Message Detail</h2>
          {detail ? <pre>{detail.payload}</pre> : <p>Select a message to inspect content.</p>}
        </div>
      </div>
    </div>
  )
}

createRoot(document.getElementById('root')).render(<App />)
