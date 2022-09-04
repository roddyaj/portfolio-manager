import { useEffect, useState } from "react";
import TitleBar from './TitleBar'
import Positions from './Positions'
import Options from './Options'
import Income from './Income'
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import './App.css';

function App() {
	const [accounts, setAccounts] = useState([]);
	const [selectedAccount, setSelectedAccount] = useState(null);
	const [portfolio, setPortfolio] = useState(null);

	useEffect(() => {
		const requestAccounts = async () => {
			const response = await fetch("http://localhost:8090/accounts");
			return await response.json();
		};
		requestAccounts().then((accounts) => {
			setAccounts(accounts);
			if (accounts.length > 0) {
				setSelectedAccount(accounts[0]);
			}
		});
	}, []);

	useEffect(() => {
		if (selectedAccount) {
			requestPortfolio(selectedAccount);
		}
	}, [selectedAccount]);

	function requestPortfolio(accountName) {
		const request = async () => {
			const response = await fetch(`http://localhost:8090/portfolio?accountName=${accountName}`);
			return await response.json();
		};
		request().then(setPortfolio);
	}

	function stopPolling() {
		const request = async () => {
			const response = await fetch("http://localhost:8090/stop-poll");
			return await response.json();
		};
		request();
	}

	return (
		<div>
			<TitleBar
				accounts={accounts}
				selectedAccount={selectedAccount}
				setSelectedAccount={setSelectedAccount}
				portfolio={portfolio}
				requestPortfolio={requestPortfolio}
				stopPolling={stopPolling}
			/>
			{
				portfolio ? (
					<div className="pm-row">
						<div className="pm-column">
							<Positions portfolio={portfolio} />
						</div>
						<div className="pm-column">
							<Options portfolio={portfolio} isLong={false} />
							<Options portfolio={portfolio} isLong={true} />
						</div>
						<div className="pm-column">
							<Income portfolio={portfolio} />
						</div>
					</div>
				) : (
					<div>No data available for {selectedAccount}.</div>
				)
			}
		</div>
	);
}

export default App;
