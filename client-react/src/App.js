import { useEffect, useState } from "react";
import './App.css';

function App() {
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [portfolio, setPortfolio] = useState(null);

  useEffect(() => {
    const requestAccounts = async () => {
      const response = await fetch("http://localhost:8090/accounts");
      const accounts = await response.json();
      return accounts;
    };
    requestAccounts().then((accounts) => {
      setAccounts(accounts);
      if (accounts.length > 0) {
        setSelectedAccount(accounts[0]);
      }
    });
  }, []);

  useEffect(() => {
    const requestPortfolio = async () => {
      const response = await fetch(`http://localhost:8090/portfolio?accountName=${selectedAccount}`);
      const accounts = await response.json();
      return accounts;
    };
    if (selectedAccount) {
      requestPortfolio().then(setPortfolio);
    }
  }, [selectedAccount]);

  const accountOptions = accounts.map((account) => {
    return (<option key={account} value={account}>{account}</option>);
  });

  return (
    <div>
      <div className="block">
        <div style={{ display: "flex" }}>
          <div className="main-header-item"></div>
          <div className="main-header-item title" style={{ textAlign: "center" }}>
            <select name="cars" id="cars" onChange={(e) => setSelectedAccount(e.target.value)}>
              {accountOptions}
            </select>
          </div>
          <div className="main-header-item" style={{ textAlign: "right" }}></div>
        </div>
      </div>
      {renderPortfolio(portfolio)}
    </div>
  );
}

function renderPortfolio(portfolio) {
  if (!portfolio) {
    return (<div>No portfolio available.</div>);
  }

  const positionRows = portfolio.positions.map((position) => {
    return (
      <tr key={position.symbol}>
        <td className="l f">{position.symbol}</td>
      </tr>
    );
  });
  return (
    <div className="row">
      <div className="column">
        <div className="block">
          <div className="heading">
            <div className="title">Positions</div>
          </div>
          <table>
            <thead>
              <tr>
                <th className="l f">Symbol</th>
              </tr>
            </thead>
            <tbody>
              {positionRows}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default App;
